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

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private MimeMessage realMimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationService, "remetente", "test@sender.com");
        ReflectionTestUtils.setField(emailNotificationService, "remetenteNome", "Test Sender");
        ReflectionTestUtils.setField(emailNotificationService, "assuntoPrefixo", "[SGC]");

        JavaMailSenderImpl realSender = new JavaMailSenderImpl();
        realMimeMessage = realSender.createMimeMessage();
    }

    @Test
    @DisplayName("Deve enviar e-mail HTML com sucesso")
    void enviarEmailHtml_deveEnviarEmailComSucesso() throws Exception {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        String to = "recipient@test.com";
        String subject = "Test Subject";
        String htmlBody = "<h1>Test Body</h1>";

        // Act
        emailNotificationService.enviarEmailHtml(to, subject, htmlBody);

        // Assert
        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(mimeMessageCaptor.capture());

        MimeMessage capturedMessage = mimeMessageCaptor.getValue();

        // Use a more robust method to check the content
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        capturedMessage.writeTo(os);
        String content = os.toString("UTF-8");

        assertEquals(to, capturedMessage.getAllRecipients()[0].toString());
        assertEquals("[SGC] Test Subject", capturedMessage.getSubject());
        assertTrue(content.contains(htmlBody), "O corpo do e-mail não contém o HTML esperado.");
        assertEquals("Test Sender <test@sender.com>", capturedMessage.getFrom()[0].toString());
        
        verify(notificacaoRepository, times(1)).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço inválido")
    void enviarEmailHtml_naoDeveEnviarParaEmailInvalido() {
        // Arrange
        String to = "invalid-email";
        String subject = "Test Subject";
        String htmlBody = "<h1>Test Body</h1>";

        // Act
        emailNotificationService.enviarEmailHtml(to, subject, htmlBody);

        // Assert
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }
}