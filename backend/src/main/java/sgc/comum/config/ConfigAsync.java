package sgc.comum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuração para execução assíncrona de tarefas.
 * Habilita o suporte a @Async no Spring.
 */
@Configuration
@EnableAsync
public class ConfigAsync {
    /**
     * Configura um pool de threads dedicado para envio de e-mails.
     * 
     * @return Executor configurado para processar e-mails de forma assíncrona
     */
    @Bean(name = "executorDeTarefasDeEmail")
    public Executor executorDeTarefasDeEmail() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // Mínimo de threads ativas
        executor.setMaxPoolSize(5);            // Máximo de threads
        executor.setQueueCapacity(100);        // Capacidade da fila de espera
        executor.setThreadNamePrefix("email-"); // Prefixo para identificação em logs
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}