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
import org.springframework.mail.javamail.JavaMailSender;
import sgc.comum.config.ConfigAplicacao;
import sgc.comum.util.Sleeper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
@Tag("unit")
@ExtendWith(MockitoExtension.class)
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

    @Mock
    private ConfigAplicacao config;

    @Mock
    private ConfigAplicacao.Email emailConfig;

    @BeforeEach
    void setUp() {
        when(config.getEmail()).thenReturn(emailConfig);
        when(emailConfig.getRemetente()).thenReturn("teste@sgc.com.br");
        when(emailConfig.getRemetenteNome()).thenReturn("SGC Teste");
        when(emailConfig.getAssuntoPrefixo()).thenReturn("[SGC]");
    }

    private static final String DESTINATARIO = "dest@teste.com";
    private static final String ASSUNTO = "Assunto";
    private static final String CORPO = "Corpo";

    @Test
    @DisplayName("Deve enviar email com sucesso na primeira tentativa")
    void enviarEmailAssincronoSucessoPrimeiraTentativa() throws ExecutionException, InterruptedException {
        // Arrange
        boolean html = false;
        
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(DESTINATARIO, ASSUNTO, CORPO, html);
        Boolean resultado = future.get();

        // Assert
        assertThat(resultado).isTrue();
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve realizar retentativas e ter sucesso")
    void enviarEmailAssincronoSucessoComRetentativa() throws ExecutionException, InterruptedException {
        // Arrange
        boolean html = false;
        
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Falha na primeira, sucesso na segunda
        doThrow(new RuntimeException("Erro SMTP temporário"))
                .doNothing()
                .when(javaMailSender).send(mimeMessage);

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(DESTINATARIO, ASSUNTO, CORPO, html);
        Boolean resultado = future.get();

        // Assert
        assertThat(resultado).isTrue();
        verify(javaMailSender, times(2)).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve falhar após exceder tentativas máximas")
    void enviarEmailAssincronoFalhaAposRetentativas() throws ExecutionException, InterruptedException {
        // Arrange
        boolean html = false;
        
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Falha sempre
        doThrow(new RuntimeException("Erro Fatal")).when(javaMailSender).send(mimeMessage);

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(DESTINATARIO, ASSUNTO, CORPO, html);
        Boolean resultado = future.get();

        // Assert
        assertThat(resultado).isFalse();
        // MAX_TENTATIVAS = 3
        verify(javaMailSender, times(3)).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve lidar com interrupção da thread durante espera")
    void enviarEmailAssincronoInterrupcaoThread() throws ExecutionException, InterruptedException {
        // Arrange
        boolean html = false;
        
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Falha na primeira tentativa
        doThrow(new RuntimeException("Erro")).when(javaMailSender).send(mimeMessage);

        // Quando tentar dormir, lança InterruptedException
        doThrow(new InterruptedException("Interrompido")).when(sleeper).sleep(anyLong());

        // Act
        CompletableFuture<Boolean> future = executor.enviarEmailAssincrono(DESTINATARIO, ASSUNTO, CORPO, html);
        Boolean resultado = future.get();

        // Assert
        // Deve falhar e sair do loop
        assertThat(resultado).isFalse();
        // Verifica que tentou enviar apenas 1 vez antes de ser interrompido
        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}
