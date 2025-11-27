package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Slf4j
@Service
@RequiredArgsConstructor
public class E2eTestDatabaseService {
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public DataSource getOrCreateDataSource(String testId) {
        return dataSources.computeIfAbsent(testId, this::createDataSource);
    }

    public void executeSqlScripts(Connection connection, Resource... scripts) {
        for (Resource script : scripts) {
            ScriptUtils.executeSqlScript(connection, script);
        }
    }

    public void setReferentialIntegrity(Connection conn, boolean enable) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY " + (enable ? "TRUE" : "FALSE"));
        }
    }

    private DataSource createDataSource(String testId) {
        try {
            log.trace("Creating new isolated DB for testId: {}", testId);

            // 1. Create a new H2 in-memory data source with a unique name
            // Initialize with schema creation in the JDBC URL
            String jdbcUrl = String.format(
                    "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS sgc", testId);

            DataSource dataSource = DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username("sa")
                    .password("")
                    .driverClassName("org.h2.Driver")
                    .build();

            try (Connection conn = dataSource.getConnection()) {
                // Set the default schema for this connection
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET SCHEMA sgc");
                    log.info("Schema sgc set as default.");
                }

                ClassPathResource schemaResource = new ClassPathResource("/schema.sql");
                if (!schemaResource.exists()) {
                    log.error("Schema SQL resource not found: /schema.sql");
                    throw new RuntimeException("Schema SQL resource not found: /schema.sql");
                }
                try {
                    executeSqlScripts(conn, schemaResource);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to execute schema.sql: " + ex.getMessage(), ex);
                }

                // Temporarily disable referential integrity for data loading
                setReferentialIntegrity(conn, false);
                ClassPathResource dataMinimalResource = new ClassPathResource("/data-minimal.sql");
                if (!dataMinimalResource.exists()) {
                    log.error("Data Minimal SQL resource not found: /data-minimal.sql");
                    throw new RuntimeException("Data Minimal SQL resource not found: /data-minimal.sql");
                }
                try {
                    executeSqlScripts(conn, dataMinimalResource);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to execute data-minimal.sql: " + ex.getMessage(), ex);
                }
                setReferentialIntegrity(conn, true);
            }

            log.debug("Successfully created isolated DB for testId: {}", testId);
            return dataSource;
        } catch (Exception e) {
            log.error("Failed to create DataSource for testId: {}", testId, e);
            throw new RuntimeException(
                    "Failed to create DataSource for testId: " + testId + " - Cause: " + e.getMessage(), e);
        }
    }

    public void cleanupDataSource(String testId) {
        DataSource dataSource = dataSources.remove(testId);
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement()) {
                stmt.execute("DROP ALL OBJECTS");
            } catch (Exception e) {
                log.error("Failed to cleanup DB objects for testId: {}", testId, e);
            }

            if (dataSource instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) dataSource).close();
                } catch (Exception e) {
                    log.error("Failed to close DataSource for testId: {}", testId, e);
                }
            }
        }
        log.debug("Cleaned up isolated DB for testId: {}", testId);
    }

    public DataSource getDataSource(String testId) {
        return dataSources.get(testId);
    }

    public void reloadDatabaseScripts(Connection conn, ResourceLoader resourceLoader) throws Exception {
        // NOTE: We do NOT re-execute schema.sql here because the schema is already
        // created
        // by Spring Boot during test context initialization. Re-executing it would
        // cause
        // errors when trying to add foreign key constraints that already exist.

        // Temporarily disable referential integrity for data loading
        setReferentialIntegrity(conn, false);

        // Execute the file of data SQL (preferring minimal if it exists)
        String sqlFilePath = "/data-minimal.sql";
        Resource dataMinimalResource = resourceLoader.getResource("classpath:" + sqlFilePath);
        Resource dataResource;

        if (dataMinimalResource.exists()) {
            dataResource = dataMinimalResource;
        } else {
            sqlFilePath = "/data.sql";
            dataResource = resourceLoader.getResource("classpath:" + sqlFilePath);
        }
        log.info("Executing SQL file: {}", sqlFilePath);
        executeSqlScripts(conn, dataResource);

        // Re-enable referential integrity
        setReferentialIntegrity(conn, true);
    }
}
