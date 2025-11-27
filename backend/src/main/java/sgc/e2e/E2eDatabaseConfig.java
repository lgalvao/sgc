package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
@Profile("e2e")
@RequiredArgsConstructor
@Slf4j
public class E2eDatabaseConfig {
    private final E2eTestDatabaseService e2eTestDatabaseService;
    private final ResourceLoader resourceLoader;

    /**
     * Defines the properties for the default E2E data source (H2).
     */
    @Bean
    @ConfigurationProperties("spring.datasource.e2e")
    public DataSourceProperties e2eDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Creates the primary DataSource bean for the application when profile 'e2e' is
     * active.
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("e2eDataSourceProperties") DataSourceProperties properties) {
        DataSource dataSource = properties.initializeDataSourceBuilder().build();
        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS SGC");
            }
            connection.setSchema("SGC");
            e2eTestDatabaseService.executeSqlScripts(connection, resourceLoader.getResource("classpath:schema.sql"));

            e2eTestDatabaseService.setReferentialIntegrity(connection, false);
            e2eTestDatabaseService.executeSqlScripts(connection, resourceLoader.getResource("classpath:data-minimal.sql"));
            e2eTestDatabaseService.setReferentialIntegrity(connection, true);
        } catch (Exception e) {
            log.error("Failed to initialize E2E data source. Original exception: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize E2E data source", e);
        }
        return dataSource;
    }
}
