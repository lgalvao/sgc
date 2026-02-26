package sgc.integracao.mocks;

import com.icegreen.greenmail.util.*;
import org.springframework.context.annotation.*;
import org.springframework.core.task.*;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.*;
import tools.jackson.databind.*;
import tools.jackson.databind.json.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Configuração compartilhada para todos os testes de integração.
 * GreenMail é sempre iniciado no perfil "test", eliminando a necessidade de um perfil "email-test" separado.
 * Testes que não verificam e-mail podem simplesmente usar @MockitoBean em EmailService.
 */
@Configuration
public class TestConfig implements AsyncConfigurer {
    @Bean(destroyMethod = "stop")
    @Profile({"test", "e2e", "secure-test"})
    public GreenMail greenMail() {
        ServerSetup setup = new ServerSetup(0, null, ServerSetup.PROTOCOL_SMTP);
        GreenMail gm = new GreenMail(setup);
        gm.start();
        return gm;
    }

    @Bean
    @Primary
    @Profile({"test", "e2e", "secure-test"})
    public JavaMailSender javaMailSender(GreenMail greenMail) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(greenMail.getSmtp().getPort());
        mailSender.setProtocol("smtp");
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        return mailSender;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    @Override
    @Bean(name = "taskExecutor")
    @Profile({"test", "e2e", "secure-test"})
    public Executor getAsyncExecutor() {
        var executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(1);
        return executor;
    }
}
