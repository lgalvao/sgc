package sgc.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.notificacao.model.Notificacao;
import sgc.notificacao.model.NotificacaoRepo;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class NotificacaoEmailServiceTest {
    private static final int LIMITE_CONTEUDO_NOTIFICACAO = 500;
    private static final int TAMANHO_CORPO_LONGO = 600;

    @Mock
    private NotificacaoRepo repositorioNotificacao;

    @Mock
    private NotificacaoEmailAsyncExecutor emailExecutor;

    @InjectMocks
    private NotificacaoEmailService notificacaoServico;

    private static final String RECIPIENT = "recipient@test.com";
    private static final String TEST_BODY = "Test body";

    @Test
    @DisplayName("Deve enviar e-mail HTML")
    void enviarEmailHtmlDeveEnviarComSucesso() {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        when(emailExecutor.enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(true));

        String para = RECIPIENT;
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        verify(emailExecutor).enviarEmailAssincrono(para, assunto, corpoHtml, true);
        
        verify(repositorioNotificacao, times(1)).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço inválido")
    void enviarEmailHtmlNaoDeveEnviarParaEnderecoInvalido() {
        String para = "invalid-email";
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        verify(emailExecutor, never()).enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean());
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve enviar e-mail de texto simples")
    void deveEnviarEmailTextoSimples() {
        // Arrange
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        when(emailExecutor.enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(true));

        String para = RECIPIENT;
        String assunto = "Test Subject Plain";
        String corpo = "This is plain text";

        // Act
        notificacaoServico.enviarEmail(para, assunto, corpo);

        // Assert
        verify(emailExecutor).enviarEmailAssincrono(para, assunto, corpo, false);
        
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
        verify(emailExecutor, never()).enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean());
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve truncar conteúdo longo da notificação")
    void deveTruncarConteudoLongoDaNotificacao() {
        // Arrange
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        when(emailExecutor.enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(true));

        String para = RECIPIENT;
        String assunto = "Test";
        String corpoLongo = "A".repeat(TAMANHO_CORPO_LONGO);

        // Act
        notificacaoServico.enviarEmail(para, assunto, corpoLongo);

        // Assert
        ArgumentCaptor<Notificacao> captorNotificacao = ArgumentCaptor.forClass(Notificacao.class);
        verify(repositorioNotificacao).save(captorNotificacao.capture());

        Notificacao notificacaoSalva = captorNotificacao.getValue();
        assertThat(notificacaoSalva.getConteudo().length()).isLessThanOrEqualTo(LIMITE_CONTEUDO_NOTIFICACAO);
        assertThat(notificacaoSalva.getConteudo()).endsWith("...");
    }

    @Test
    @DisplayName("Deve logar erro quando envio falha")
    void deveLogarErroQuandoEnvioFalha() {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        CompletableFuture<Boolean> futureFalho = CompletableFuture.completedFuture(false);
        when(emailExecutor.enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(futureFalho);

        String para = RECIPIENT;
        String assunto = "Test";
        String corpo = TEST_BODY;

        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(emailExecutor).enviarEmailAssincrono(para, assunto, corpo, false);
    }

    @Test
    @DisplayName("Deve logar erro quando exception ocorre no CompletableFuture")
    void deveLogarErroQuandoExceptionOcorre() {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        CompletableFuture<Boolean> futureComErro = new CompletableFuture<>();
        futureComErro.completeExceptionally(new RuntimeException("Erro de teste"));
        when(emailExecutor.enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(futureComErro);

        String para = RECIPIENT;
        String assunto = "Test";
        String corpo = TEST_BODY;

        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(emailExecutor).enviarEmailAssincrono(para, assunto, corpo, false);
    }

    @Test
    @DisplayName("Deve capturar RuntimeException durante processamento")
    void deveCapturaRuntimeException() {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenThrow(new RuntimeException("Erro de teste"));

        String para = RECIPIENT;
        String assunto = "Test";
        String corpo = TEST_BODY;

        // Não deve lançar exceção, apenas logar
        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(repositorioNotificacao).save(any(Notificacao.class));
        verify(emailExecutor, never()).enviarEmailAssincrono(anyString(), anyString(), anyString(), anyBoolean());
    }
}
