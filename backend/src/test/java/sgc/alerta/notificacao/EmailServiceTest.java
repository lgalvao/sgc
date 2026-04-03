package sgc.alerta.notificacao;

import jakarta.mail.internet.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.mail.javamail.*;
import sgc.alerta.*;
import sgc.comum.config.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway.Init")
class EmailServiceTest {
    private static final String DESTINATARIO = "recipient@test.com";
    @Mock
    private JavaMailSender enviadorEmail;
    @Mock
    private ConfigAplicacao config;
    @InjectMocks
    private EmailService notificacaoServico;

    private void setupMockEmail() {
        ConfigAplicacao.Email emailConfig = new ConfigAplicacao.Email();
        emailConfig.setRemetente("noreply@test.com");
        emailConfig.setRemetenteNome("Remetente teste");
        emailConfig.setAssuntoPrefixo("[Teste]");
        when(config.getEmail()).thenReturn(emailConfig);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(enviadorEmail.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar e-mail HTML")
    void enviarEmailHtmlDeveEnviarComSucesso() {
        setupMockEmail();

        String assunto = "Test subject";
        String corpoHtml = "<h1>Test body</h1>";

        notificacaoServico.enviarEmailHtml(DESTINATARIO, assunto, corpoHtml);

        verify(enviadorEmail, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço inválido")
    void enviarEmailHtmlNaoDeveEnviarParaEnderecoInvalido() {
        String para = "invalid-email";
        String assunto = "Test subject";
        String corpoHtml = "<h1>Test body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        verify(enviadorEmail, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve enviar e-mail de texto simples")
    void deveEnviarEmailTextoSimples() {
        setupMockEmail();

        String assunto = "Test subject plain";
        String corpo = "This is plain text";

        notificacaoServico.enviarEmail(DESTINATARIO, assunto, corpo);

        verify(enviadorEmail, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço vazio")
    void naoDeveEnviarEmailParaEnderecoVazio() {
        String para = "";
        String assunto = "Test";
        String corpo = "Body";

        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(enviadorEmail, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve aceitar e-mail válido com espaços nas bordas")
    void deveAceitarEmailValidoComEspacosNasBordas() {
        setupMockEmail();

        notificacaoServico.enviarEmail("  recipient@test.com  ", "Assunto", "Corpo");

        verify(enviadorEmail).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve encapsular erro de SMTP em RuntimeException")
    void deveEncapsularErroDeSmtpEmRuntimeException() {
        setupMockEmail();

        assertThatThrownBy(() -> notificacaoServico.enviarEmail("a..b@test.com", "Assunto", "Corpo"))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(AddressException.class);

        verify(enviadorEmail, never()).send(any(MimeMessage.class));
    }
}
