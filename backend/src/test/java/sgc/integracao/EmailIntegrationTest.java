package sgc.integracao;

import com.icegreen.greenmail.util.GreenMail;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import sgc.Sgc;
import sgc.integracao.config.EmailTestConfig;
import sgc.notificacao.NotificacaoEmailService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles({"test", "email-test"})
@Import(EmailTestConfig.class)
@Tag("integration")
@DisplayName("Integração: Envio de E-mail com GreenMail")
class EmailIntegrationTest {

    @Autowired
    private NotificacaoEmailService emailService;

    @Autowired
    private GreenMail greenMail;

    @BeforeEach
    void setup() {
        greenMail.reset();
    }

    @Test
    @DisplayName("Deve enviar e-mail real para o servidor local GreenMail e permitir verificação")
    void deveEnviarEmailRealParaGreenMail() throws Exception {
        // Arrange
        String destinatario = "usuario@teste.com";
        String assunto = "Assunto de Teste";
        String corpo = "Email de teste real";

        // Act
        emailService.enviarEmail(destinatario, assunto, corpo);

        // Assert: GreenMail capturou a mensagem
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
