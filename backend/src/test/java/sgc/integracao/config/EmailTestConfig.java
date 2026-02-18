package sgc.integracao.config;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@TestConfiguration
public class EmailTestConfig {

    @Bean(destroyMethod = "stop")
    public GreenMail greenMail() {
        // Usa porta dinâmica (0) para evitar conflitos entre contextos de teste
        ServerSetup setup = new ServerSetup(0, null, ServerSetup.PROTOCOL_SMTP);
        GreenMail gm = new GreenMail(setup);
        gm.start();
        return gm;
    }

    @Bean
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
}
