package sgc.comum.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "sgc.sgrh.repository",
    entityManagerFactoryRef = "sgrhEntityManagerFactory",
    transactionManagerRef = "sgrhTransactionManager"
)
@ConditionalOnProperty(name = "spring.sgrh.datasource.url")
public class SgrhDataSourceConfig {
    
    /**
     * Propriedades do datasource SGRH.
     */
    @Bean
    @ConfigurationProperties("spring.sgrh.datasource")
    public DataSourceProperties sgrhDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    /**
     * Datasource SGRH (Oracle).
     * Configurado como read-only com pool reduzido.
     */
    @Bean(name = "sgrhDataSource")
    public DataSource sgrhDataSource(
            @Qualifier("sgrhDataSourceProperties") DataSourceProperties properties) {
        
        HikariDataSource dataSource = properties
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
        
        // Configurações específicas para read-only
        dataSource.setReadOnly(true);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        
        // Pool name para identificação em logs
        dataSource.setPoolName("SGRH-Oracle-Pool");
        
        return dataSource;
    }
    
    /**
     * EntityManagerFactory para entidades SGRH.
     */
    @Bean(name = "sgrhEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sgrhEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sgrhDataSource") DataSource dataSource) {
        
        // Propriedades específicas do Hibernate para Oracle
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        properties.put("hibernate.hbm2ddl.auto", "none"); // NUNCA alterar views
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", false);
        properties.put("hibernate.jdbc.batch_size", 20);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        
        // Cache de segundo nível (opcional) - Desabilitado temporariamente devido a erro de inicialização do provedor de cache
        properties.put("hibernate.cache.use_second_level_cache", false);
        properties.put("hibernate.cache.use_query_cache", false);
        // properties.put("hibernate.cache.region.factory_class", 
        //     "org.hibernate.cache.jcache.JCacheRegionFactory");
        
        return builder
            .dataSource(dataSource)
            .packages("sgc.sgrh.entity")
            .persistenceUnit("sgrh")
            .properties(properties)
            .build();
    }
    
    /**
     * TransactionManager para operações SGRH.
     * Todas as transações são read-only.
     */
    @Bean(name = "sgrhTransactionManager")
    public PlatformTransactionManager sgrhTransactionManager(
            @Qualifier("sgrhEntityManagerFactory") EntityManagerFactory emf) {
        
        JpaTransactionManager transactionManager = new JpaTransactionManager(emf);
        transactionManager.setDefaultTimeout(30); // 30 segundos timeout
        
        return transactionManager;
    }
}