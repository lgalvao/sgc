package sgc.comum.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
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
    entityManagerFactoryRef = "fabricaDeGerenciadoresDeEntidadeSgrh",
    transactionManagerRef = "gerenciadorDeTransacoesSgrh"
)
@ConditionalOnProperty(name = "spring.sgrh.datasource.url")
public class ConfiguracaoFonteDadosSgrh {

    /**
     * Propriedades da fonte de dados (datasource) do SGRH.
     */
    @Bean(name = "propriedadesDaFonteDeDadosSgrh")
    @ConfigurationProperties("spring.sgrh.datasource")
    public DataSourceProperties propriedadesDaFonteDeDadosSgrh() {
        return new DataSourceProperties();
    }

    /**
     * Fonte de dados (datasource) do SGRH (Oracle).
     * Configurado como somente leitura com um pool de conexões reduzido.
     */
    @Bean(name = "fonteDeDadosSgrh")
    public DataSource fonteDeDadosSgrh(
            @Qualifier("propriedadesDaFonteDeDadosSgrh") DataSourceProperties propriedades) {

        HikariDataSource dataSource = propriedades
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();

        // Configurações específicas para somente leitura
        dataSource.setReadOnly(true);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        // Nome do pool para identificação em logs
        dataSource.setPoolName("SGRH-Oracle-Pool");

        return dataSource;
    }

    /**
     * Fábrica de Gerenciadores de Entidade (EntityManagerFactory) para as entidades do SGRH.
     */
    @Bean(name = "fabricaDeGerenciadoresDeEntidadeSgrh")
    public LocalContainerEntityManagerFactoryBean fabricaDeGerenciadoresDeEntidadeSgrh(
            EntityManagerFactoryBuilder builder,
            @Qualifier("fonteDeDadosSgrh") DataSource dataSource) {

        // Propriedades específicas do Hibernate para Oracle
        Map<String, Object> propriedades = new HashMap<>();
        propriedades.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        propriedades.put("hibernate.hbm2ddl.auto", "none"); // NUNCA alterar views
        propriedades.put("hibernate.show_sql", false);
        propriedades.put("hibernate.format_sql", false);
        propriedades.put("hibernate.jdbc.batch_size", 20);
        propriedades.put("hibernate.order_inserts", true);
        propriedades.put("hibernate.order_updates", true);

        // Cache de segundo nível (opcional) - Desabilitado temporariamente
        propriedades.put("hibernate.cache.use_second_level_cache", false);
        propriedades.put("hibernate.cache.use_query_cache", false);

        return builder
            .dataSource(dataSource)
            .packages("sgc.sgrh.entity")
            .persistenceUnit("sgrh")
            .properties(propriedades)
            .build();
    }

    /**
     * Gerenciador de Transações (TransactionManager) para operações do SGRH.
     * Todas as transações são somente leitura.
     */
    @Bean(name = "gerenciadorDeTransacoesSgrh")
    public PlatformTransactionManager gerenciadorDeTransacoesSgrh(
            @Qualifier("fabricaDeGerenciadoresDeEntidadeSgrh") EntityManagerFactory emf) {

        JpaTransactionManager transactionManager = new JpaTransactionManager(emf);
        transactionManager.setDefaultTimeout(30); // Timeout de 30 segundos

        return transactionManager;
    }
}