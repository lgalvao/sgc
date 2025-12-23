package sgc.notificacao;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.notificacao.model.Notificacao;
import sgc.notificacao.model.NotificacaoRepo;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoEmailServiceTest {
    @Mock
    private JavaMailSender enviadorDeEmail;

    @Mock
    private NotificacaoRepo repositorioNotificacao;

    @InjectMocks
    private NotificacaoEmailService notificacaoServico;

    private MimeMessage mimeMessageReal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificacaoServico, "remetente", "test@sender.com");
        ReflectionTestUtils.setField(notificacaoServico, "nomeRemetente", "Test Sender");
        ReflectionTestUtils.setField(notificacaoServico, "prefixoAssunto", "[SGC]");

        JavaMailSenderImpl senderReal = new JavaMailSenderImpl();
        mimeMessageReal = senderReal.createMimeMessage();
    }

    @Test
    @DisplayName("Deve enviar e-mail HTML")
    void enviarEmailHtml_deveEnviarComSucesso() throws Exception {
        when(enviadorDeEmail.createMimeMessage()).thenReturn(mimeMessageReal);
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        String para = "recipient@test.com";
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        ArgumentCaptor<MimeMessage> captorMimeMessage = ArgumentCaptor.forClass(MimeMessage.class);
        verify(enviadorDeEmail).send(captorMimeMessage.capture());

        MimeMessage mensagemCapturada = captorMimeMessage.getValue();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mensagemCapturada.writeTo(os);
        String conteudo = os.toString(StandardCharsets.UTF_8);

        assertEquals(para, mensagemCapturada.getAllRecipients()[0].toString());
        assertEquals("[SGC] Test Subject", mensagemCapturada.getSubject());
        assertTrue(conteudo.contains(corpoHtml), "O corpo do e-mail não contém o HTML esperado.");
        assertEquals("Test Sender <test@sender.com>", mensagemCapturada.getFrom()[0].toString());

        verify(repositorioNotificacao, times(1)).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço inválido")
    void enviarEmailHtml_naoDeveEnviarParaEnderecoInvalido() {
        String para = "invalid-email";
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        verify(enviadorDeEmail, never()).createMimeMessage();
        verify(enviadorDeEmail, never()).send(any(MimeMessage.class));
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve enviar e-mail de texto simples")
    void deveEnviarEmailTextoSimples() throws Exception {
        // Arrange
        when(enviadorDeEmail.createMimeMessage()).thenReturn(mimeMessageReal);
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        String para = "recipient@test.com";
        String assunto = "Test Subject Plain";
        String corpo = "This is plain text";

        // Act
        notificacaoServico.enviarEmail(para, assunto, corpo);

        // Assert
        ArgumentCaptor<MimeMessage> captorMimeMessage = ArgumentCaptor.forClass(MimeMessage.class);
        verify(enviadorDeEmail).send(captorMimeMessage.capture());

        MimeMessage mensagemCapturada = captorMimeMessage.getValue();
        assertEquals(para, mensagemCapturada.getAllRecipients()[0].toString());
        assertEquals("[SGC] Test Subject Plain", mensagemCapturada.getSubject());
        verify(repositorioNotificacao, times(1)).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço vazio")
    void naoDeveEnviarEmailParaEnderecoVazio() {
        // Arrange
        String para = "";
        String assunto = "Test";
        String corpo = "Body";

        // Act
        notificacaoServico.enviarEmail(para, assunto, corpo);

        // Assert
        verify(enviadorDeEmail, never()).createMimeMessage();
        verify(enviadorDeEmail, never()).send(any(MimeMessage.class));
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço null")
    void naoDeveEnviarEmailParaEnderecoNull() {
        // Arrange
        String para = null;
        String assunto = "Test";
        String corpo = "Body";

        // Act
        notificacaoServico.enviarEmail(para, assunto, corpo);

        // Assert
        verify(enviadorDeEmail, never()).createMimeMessage();
        verify(enviadorDeEmail, never()).send(any(MimeMessage.class));
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve truncar conteúdo longo da notificação")
    void deveTruncarConteudoLongoDaNotificacao() throws Exception {
        // Arrange
        when(enviadorDeEmail.createMimeMessage()).thenReturn(mimeMessageReal);
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        String para = "recipient@test.com";
        String assunto = "Test";
        String corpoLongo = "A".repeat(600); // Mais que o limite de 500

        // Act
        notificacaoServico.enviarEmail(para, assunto, corpoLongo);

        // Assert
        ArgumentCaptor<Notificacao> captorNotificacao = ArgumentCaptor.forClass(Notificacao.class);
        verify(repositorioNotificacao).save(captorNotificacao.capture());

        Notificacao notificacaoSalva = captorNotificacao.getValue();
        assertTrue(notificacaoSalva.getConteudo().length() <= 500);
        assertTrue(notificacaoSalva.getConteudo().endsWith("..."));
    }
}
