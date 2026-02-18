package sgc.integracao;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
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
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles({"test", "email-test"})
@Import({EmailTestConfig.class, TestSecurityConfig.class, TestThymeleafConfig.class})
@Tag("integration")
@DisplayName("Integração: Notificações de Processo com GreenMail")
class ProcessoEmailIntegrationTest {

    @Autowired
    private NotificacaoEmailService emailService;

    @Autowired
    private NotificacaoModelosService modelosService;

    @Autowired
    private GreenMail greenMail;

    @BeforeEach
    void setup() {
        greenMail.reset();
    }

    @Test
    @DisplayName("Deve enviar e-mail HTML real usando template e verificar no GreenMail")
    void deveEnviarEmailHtmlRealComTemplate() throws Exception {
        // Arrange
        String destinatario = "chefe@unidade.com";
        String nomeUnidade = "Secretaria de Testes";
        String nomeProcesso = "Revisão Periódica 2026";
        LocalDateTime dataLimite = LocalDateTime.now().plusDays(15);
        
        // Act: Gerar HTML real via Thymeleaf
        String html = modelosService.criarEmailProcessoIniciado(
                nomeUnidade, nomeProcesso, "REVISAO", dataLimite);
        
        // Enviar via serviço real (conectado ao GreenMail pelo perfil email-test)
        emailService.enviarEmailHtml(destinatario, "Início de Processo", html);

        // Assert: Aguardar e verificar
        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
        
        MimeMessage[] mensagens = greenMail.getReceivedMessages();
        assertThat(mensagens).hasSize(1);
        
        MimeMessage msg = mensagens[0];
        assertThat(msg.getAllRecipients()[0]).hasToString(destinatario);
        
        String corpoRecebido = GreenMailUtil.getBody(msg);
        assertThat(msg.getSubject()).contains("Início de Processo");
        assertThat(corpoRecebido)
            .contains(nomeUnidade)
            .contains(nomeProcesso)
            .contains("</html>");
    }
}
