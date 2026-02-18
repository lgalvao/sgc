package sgc.integracao;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.notificacao.NotificacaoEmailService;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DisplayName("Integração: Envio de E-mail com GreenMail")
class EmailIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificacaoEmailService emailService;

    // greenMail is already autowired in BaseIntegrationTest

    @Autowired
    private org.springframework.mail.javamail.JavaMailSenderImpl javaMailSender;

    @BeforeEach
    void setupChild() {
        if (greenMail != null) {
            greenMail.reset();
            // Sync port to avoid mismatches if GreenMail restarted
            javaMailSender.setPort(greenMail.getSmtp().getPort());
        }
    }

    @Test
    @DisplayName("Deve enviar e-mail real para o servidor local GreenMail e permitir verificação")
    void deveEnviarEmailRealParaGreenMail() throws Exception {
        // Verify port configuration
        int port = greenMail.getSmtp().getPort();
        assertThat(javaMailSender.getPort()).isEqualTo(port);
        System.out.println("GreenMail Port: " + port);

        // Arrange
        String destinatario = "usuario@teste.com";
        String assunto = "Assunto de Teste";
        String corpo = "Email de teste real";

        // Act
        emailService.enviarEmail(destinatario, assunto, corpo);

        // Assert: GreenMail capturou a mensagem
        // Aguarda até 5s pela chegada de 1 email
        aguardarEmail(1);
        
        MimeMessage[] mensagens = greenMail.getReceivedMessages();
        assertThat(mensagens).hasSize(1);
        
        MimeMessage msg = mensagens[0];
        assertThat(msg.getAllRecipients()[0].toString()).isEqualTo(destinatario);
        assertThat(msg.getSubject()).contains(assunto);
        
        // Usar utilitário do GreenMail para capturar o corpo de forma robusta
        String corpoRecebido = com.icegreen.greenmail.util.GreenMailUtil.getBody(msg);
        assertThat(corpoRecebido).contains(corpo);
    }
}
