package sgc.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("e2e")
@RequiredArgsConstructor
public class E2eDatabaseConfig implements WebMvcConfigurer {

    private final E2eTestRequestInterceptor e2eTestRequestInterceptor;
    private final E2eTestDatabaseService e2eTestDatabaseService;
    private final ResourceLoader resourceLoader;

    /**
     * Registers the interceptor to capture the X-Test-ID header.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(e2eTestRequestInterceptor);
    }

    /**
     * Defines the properties for the default E2E data source (H2).
     */
    @Bean
    @ConfigurationProperties("spring.datasource.e2e")
    public DataSourceProperties e2eDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Creates the default DataSource bean used when no X-Test-ID is present.
     */
    @Bean
    public DataSource defaultE2eDataSource(@Qualifier("e2eDataSourceProperties") DataSourceProperties properties) {
        DataSource defaultDataSource = properties.initializeDataSourceBuilder().build();
        // Initialize the default data source with schema and minimal data
        try (Connection connection = defaultDataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, resourceLoader.getResource("classpath:schema-h2.sql"));
            // ScriptUtils.executeSqlScript(connection, resourceLoader.getResource("classpath:data-h2-minimal.sql"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize default E2E data source", e);
        }
        return defaultDataSource;
    }

    /**
     * The primary DataSource bean for the application when profile 'e2e' is active.
     * This is a routing DataSource that delegates to the correct in-memory H2 database
     * based on the X-Test-ID header captured by the interceptor.
     */
    @Bean
    @Primary
    public DataSource dataSource(DataSource defaultE2eDataSource) {
        E2eDataSourceRouter router = new E2eDataSourceRouter(e2eTestDatabaseService);
        router.setDefaultDataSource(defaultE2eDataSource); // Set the default data source

        // Provide a dummy targetDataSources map to satisfy AbstractRoutingDataSource's requirement
        // The actual routing logic is in determineTargetDataSource
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("dummy", defaultE2eDataSource); // Add at least one entry
        router.setTargetDataSources(targetDataSources);

        router.afterPropertiesSet(); // Ensures the router is initialized
        return router;
    }
}
