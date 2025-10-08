package sgc.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuração específica para testes que precisam de contexto Spring Boot.
 * Esta configuração garante que os repositórios JPA sejam habilitados corretamente
 * e que o contexto de teste seja carregado adequadamente.
 */
@TestConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "sgc")
@EnableJpaRepositories(basePackages = "sgc")
@EnableTransactionManagement
public class TestConfig {

    /**
     * Bean vazio para evitar conflitos de configuração.
     * Pode ser usado para adicionar configurações específicas de teste se necessário.
     */
    @Bean
    @Primary
    public Object testBean() {
        return new Object();
    }
}