package sgc.integracao.config;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@TestConfiguration
public class EmailTestConfig {

    private GreenMail greenMail;

    @PostConstruct
    public void startGreenMail() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
    }

    @PreDestroy
    public void stopGreenMail() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    @Bean
    public GreenMail greenMail() {
        return greenMail;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(ServerSetupTest.SMTP.getPort());
        mailSender.setProtocol("smtp");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");

        return mailSender;
    }

    /**
     * Helper para limpar as mensagens recebidas entre os testes.
     */
    public void limparMensagens() {
        try {
            greenMail.purgeEmailFromAllMailboxes();
        } catch (FolderException e) {
            throw new RuntimeException("Falha ao limpar e-mails do GreenMail", e);
        }
    }

    /**
     * Retorna as mensagens recebidas.
     */
    public MimeMessage[] getMensagensRecebidas() {
        return greenMail.getReceivedMessages();
    }
}
