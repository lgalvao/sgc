package sgc.integracao.mocks;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import sgc.notificacao.NotificacaoEmailAsyncExecutor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class TestConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    @Bean
    @Profile({"test", "e2e", "secure-test"})
    public NotificacaoEmailAsyncExecutor notificacaoEmailAsyncExecutor() {
        return Mockito.mock(NotificacaoEmailAsyncExecutor.class);
    }
}
