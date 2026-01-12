package sgc.notificacao;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.util.Sleeper;
import sgc.notificacao.dto.EmailDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificacaoEmailAsyncExecutor")
class NotificacaoEmailAsyncExecutorTest {

    @InjectMocks
    private NotificacaoEmailAsyncExecutor executor;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private Sleeper sleeper;

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

    @Test
    @DisplayName("Deve lidar com interrupção da thread durante espera")
    void enviarEmailAssincrono_interrupcaoThread() throws ExecutionException, InterruptedException {
        // Arrange
        EmailDto email = new EmailDto("dest@teste.com", "Assunto", "Corpo", false);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Falha na primeira tentativa
        doThrow(new RuntimeException("Erro")).when(javaMailSender).send(mimeMessage);

        // Quando tentar dormir, lança InterruptedException
        doThrow(new InterruptedException("Interrompido")).when(sleeper).sleep(anyLong());

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(email);
        Boolean resultado = future.get();

        // Assert
        // Deve falhar e sair do loop
        assertThat(resultado).isFalse();
        // Verifica que tentou enviar apenas 1 vez antes de ser interrompido
        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}
