package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
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

    public void executeSqlScripts(Connection connection, Resource... scripts) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            for (Resource scriptResource : scripts) {
                String sql = new BufferedReader(
                        new InputStreamReader(scriptResource.getInputStream()))
                        .lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining("\n"));

                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        log.trace("Executing SQL statement: {}", trimmed);
                        stmt.execute(trimmed);
                    }
                }
            }
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
            String jdbcUrl = String.format(
                    "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS SGC",
                    testId
            );

            DataSource dataSource = DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username("sa")
                    .password("")
                    .driverClassName("org.h2.Driver")
                    .build();

            try (Connection conn = dataSource.getConnection()) {
                conn.setSchema("SGC");
                executeSqlScripts(conn, new ClassPathResource("/schema.sql"));

                // Temporarily disable referential integrity for data loading
                setReferentialIntegrity(conn, false);
                executeSqlScripts(conn, new ClassPathResource("/data-minimal.sql"));
                setReferentialIntegrity(conn, true);


                // Programmatic insertion of user '1' removed as it is already in data-minimal.sql

                // Explicitly commit the transaction
                // conn.commit(); // Removed due to auto-commit being true by default on H2 connections


            }

            log.debug("Successfully created isolated DB for testId: {}", testId);
            return dataSource;
        } catch (Exception e) {
            log.error("Failed to create DataSource for testId: {}", testId, e);
            throw new RuntimeException("Failed to create DataSource for testId: " + testId, e);
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
