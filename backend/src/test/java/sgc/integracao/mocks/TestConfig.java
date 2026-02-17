package sgc.integracao.mocks;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import sgc.notificacao.NotificacaoEmailAsyncExecutor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Executor;

@Configuration
public class TestConfig implements AsyncConfigurer {
    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    @Bean
    @Primary
    @Profile({"test", "e2e", "secure-test"})
    public NotificacaoEmailAsyncExecutor notificacaoEmailAsyncExecutor() {
        return Mockito.mock(NotificacaoEmailAsyncExecutor.class);
    }

    /**
     * Configuração para executar métodos @Async de forma síncrona em testes.
     */
    @Override
    @Bean(name = "taskExecutor")
    @Profile({"test", "e2e", "secure-test"})
    public Executor getAsyncExecutor() {
        return new SyncTaskExecutor();
    }
}
