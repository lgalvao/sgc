package sgc.comum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
/**
 * Configuração para execução assíncrona de tarefas.
 * Habilita o suporte a @Async no Spring.
 */
@Configuration
@EnableAsync
public class ConfigAsync {
    // A anotação @EnableAsync já configura um executor de tarefas padrão.
    // A configuração explícita de um bean ThreadPoolTaskExecutor foi removida
    // para simplificar a configuração, conforme sugerido pela revisão de código
    // que indicava um possível "overengineering". O executor padrão do Spring Boot
    // é suficiente para as necessidades atuais da aplicação.
}