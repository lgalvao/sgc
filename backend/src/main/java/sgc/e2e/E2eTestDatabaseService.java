package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet; // Added this import
import java.sql.SQLException; // Added this import
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class E2eTestDatabaseService {
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public DataSource getOrCreateDataSource(String testId) {
        return dataSources.computeIfAbsent(testId, this::createDataSource);
    }

    private void executeSqlScript(Connection connection, String scriptPath) throws Exception {
        String sql = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(scriptPath))))
                .lines()
                .collect(Collectors.joining("\n"));
        try (Statement stmt = connection.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    log.trace("Executing SQL statement: {}", trimmed);
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException e) {
                        log.error("Error executing SQL statement: {}", trimmed, e);
                    }
                }
            }
        }
    }

    private DataSource createDataSource(String testId) {
        try {
            log.trace("Creating new isolated DB for testId: {}", testId);

            // 1. Create a new H2 in-memory data source with a unique name
            String jdbcUrl = String.format(
                    "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1",
                    testId
            );

            DataSource dataSource = DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username("sa")
                    .password("")
                    .driverClassName("org.h2.Driver")
                    .build();

            try (Connection conn = dataSource.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE SCHEMA SGC");
                }
                conn.setSchema("SGC");
                executeSqlScript(conn, "/schema.sql");

                // Temporarily disable referential integrity for data loading
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
                }
                executeSqlScript(conn, "/data-minimal.sql");

                // Re-enable referential integrity
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                }

                // Programmatic insertion of user '1' and 'SERVIDOR' profile for E2E tests
                try (Statement stmt = conn.createStatement()) {
                    String insertUserSql = "INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES ('1', 'Ana Paula Souza', 'ana.souza@tre-pe.jus.br', '1234', 10)";
                    log.trace("Programmatically inserting user '1': {}", insertUserSql);
                    stmt.execute(insertUserSql);

                    String insertProfileSql = "INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES ('1', 'SERVIDOR')";
                    log.debug("Programmatically inserting profile for user '1': {}", insertProfileSql);
                    stmt.execute(insertProfileSql);

                    // Verify programmatic insert
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM SGC.USUARIO WHERE TITULO_ELEITORAL = '1'")) {
                        if (rs.next()) {
                            log.info("DEBUG (Programmatic): Count for USUARIO with TITULO_ELEITORAL '1' after programmatic insert: {}", rs.getInt(1));
                        }
                    }
                }

                // Explicitly commit the transaction
                conn.commit();


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
                log.error("Failed to cleanup DB for testId: {}", testId, e);
            }
        }
        log.debug("Cleaned up isolated DB for testId: {}", testId);
    }

    public DataSource getDataSource(String testId) {
        return dataSources.get(testId);
    }
}
