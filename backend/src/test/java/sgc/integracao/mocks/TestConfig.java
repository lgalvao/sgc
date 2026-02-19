package sgc.integracao.mocks;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Configuração compartilhada para todos os testes de integração.
 * GreenMail é sempre iniciado no perfil "test", eliminando a necessidade de um perfil "email-test" separado.
 * Testes que não verificam e-mail podem simplesmente usar @MockitoBean em NotificacaoEmailService.
 */
@Configuration
public class TestConfig implements AsyncConfigurer {

    // --- GreenMail: sempre disponível em testes ---
    @Bean(destroyMethod = "stop")
    @Profile({"test", "e2e", "secure-test"})
    public GreenMail greenMail() {
        ServerSetup setup = new ServerSetup(0, null, ServerSetup.PROTOCOL_SMTP);
        GreenMail gm = new GreenMail(setup);
        gm.start();
        return gm;
    }

    // --- JavaMailSender real conectado ao GreenMail ---
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

    // --- ObjectMapper ---
    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    /**
     * Executa métodos @Async de forma síncrona em testes.
     */
    @Override
    @Bean(name = "taskExecutor")
    @Profile({"test", "e2e", "secure-test"})
    public Executor getAsyncExecutor() {
        return new SyncTaskExecutor();
    }
}
