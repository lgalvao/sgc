package sgc.notificacao;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.notificacao.dto.EmailDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: NotificacaoEmailAsyncExecutor")
class NotificacaoEmailAsyncExecutorTest {

    @InjectMocks
    private NotificacaoEmailAsyncExecutor executor;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(executor, "remetente", "teste@sgc.com.br");
        ReflectionTestUtils.setField(executor, "nomeRemetente", "SGC Teste");
        ReflectionTestUtils.setField(executor, "prefixoAssunto", "[SGC]");
    }

    @Test
    @DisplayName("Deve enviar email com sucesso na primeira tentativa")
    void enviarEmailAssincrono_sucessoPrimeiraTentativa() throws ExecutionException, InterruptedException {
        // Arrange
        EmailDto email = new EmailDto("dest@teste.com", "Assunto", "Corpo", false);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(email);
        Boolean resultado = future.get();

        // Assert
        assertThat(resultado).isTrue();
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve realizar retentativas e ter sucesso")
    void enviarEmailAssincrono_sucessoComRetentativa() throws ExecutionException, InterruptedException {
        // Arrange
        EmailDto email = new EmailDto("dest@teste.com", "Assunto", "Corpo", false);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Falha na primeira, sucesso na segunda
        doThrow(new RuntimeException("Erro SMTP temporário"))
                .doNothing()
                .when(javaMailSender).send(mimeMessage);

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(email);
        Boolean resultado = future.get();

        // Assert
        assertThat(resultado).isTrue();
        verify(javaMailSender, times(2)).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve falhar após exceder tentativas máximas")
    void enviarEmailAssincrono_falhaAposRetentativas() throws ExecutionException, InterruptedException {
        // Arrange
        EmailDto email = new EmailDto("dest@teste.com", "Assunto", "Corpo", false);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Falha sempre
        doThrow(new RuntimeException("Erro Fatal")).when(javaMailSender).send(mimeMessage);

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(email);
        Boolean resultado = future.get();

        // Assert
        assertThat(resultado).isFalse();
        // MAX_TENTATIVAS = 3
        verify(javaMailSender, times(3)).send(mimeMessage);
    }
}
