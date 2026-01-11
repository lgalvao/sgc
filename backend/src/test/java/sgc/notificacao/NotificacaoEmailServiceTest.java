package sgc.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import sgc.notificacao.dto.EmailDto;
import sgc.notificacao.model.Notificacao;
import sgc.notificacao.model.NotificacaoRepo;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class NotificacaoEmailServiceTest {
    
    private static final int LIMITE_CONTEUDO_NOTIFICACAO = 500;
    private static final int TAMANHO_CORPO_LONGO = 600;
    
    @Mock
    private JavaMailSender enviadorDeEmail;

    @Mock
    private NotificacaoRepo repositorioNotificacao;

    @Mock
    private NotificacaoEmailAsyncExecutor emailExecutor;

    @InjectMocks
    private NotificacaoEmailService notificacaoServico;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve enviar e-mail HTML")
    void enviarEmailHtml_deveEnviarComSucesso() throws Exception {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        when(emailExecutor.enviarEmailAssincrono(any(EmailDto.class))).thenReturn(CompletableFuture.completedFuture(true));

        String para = "recipient@test.com";
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        ArgumentCaptor<EmailDto> captorEmailDto = ArgumentCaptor.forClass(EmailDto.class);
        verify(emailExecutor).enviarEmailAssincrono(captorEmailDto.capture());

        EmailDto emailCapturado = captorEmailDto.getValue();
        assertEquals(para, emailCapturado.getDestinatario());
        assertEquals(assunto, emailCapturado.getAssunto());
        assertTrue(emailCapturado.isHtml());
        assertEquals(corpoHtml, emailCapturado.getCorpo());

        verify(repositorioNotificacao, times(1)).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço inválido")
    void enviarEmailHtml_naoDeveEnviarParaEnderecoInvalido() {
        String para = "invalid-email";
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        verify(emailExecutor, never()).enviarEmailAssincrono(any());
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve enviar e-mail de texto simples")
    void deveEnviarEmailTextoSimples() throws Exception {
        // Arrange
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        when(emailExecutor.enviarEmailAssincrono(any(EmailDto.class))).thenReturn(CompletableFuture.completedFuture(true));

        String para = "recipient@test.com";
        String assunto = "Test Subject Plain";
        String corpo = "This is plain text";

        // Act
        notificacaoServico.enviarEmail(para, assunto, corpo);

        // Assert
        ArgumentCaptor<EmailDto> captorEmailDto = ArgumentCaptor.forClass(EmailDto.class);
        verify(emailExecutor).enviarEmailAssincrono(captorEmailDto.capture());

        EmailDto emailCapturado = captorEmailDto.getValue();
        assertEquals(para, emailCapturado.getDestinatario());
        assertEquals(assunto, emailCapturado.getAssunto());
        org.junit.jupiter.api.Assertions.assertFalse(emailCapturado.isHtml());
        assertEquals(corpo, emailCapturado.getCorpo());

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
        verify(emailExecutor, never()).enviarEmailAssincrono(any());
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
        verify(emailExecutor, never()).enviarEmailAssincrono(any());
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve truncar conteúdo longo da notificação")
    void deveTruncarConteudoLongoDaNotificacao() throws Exception {
        // Arrange
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        when(emailExecutor.enviarEmailAssincrono(any(EmailDto.class))).thenReturn(CompletableFuture.completedFuture(true));

        String para = "recipient@test.com";
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
    void deveLogarErroQuandoEnvioFalha() throws Exception {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        CompletableFuture<Boolean> futureFalho = CompletableFuture.completedFuture(false);
        when(emailExecutor.enviarEmailAssincrono(any(EmailDto.class))).thenReturn(futureFalho);

        String para = "recipient@test.com";
        String assunto = "Test";
        String corpo = "Test body";

        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(emailExecutor).enviarEmailAssincrono(any(EmailDto.class));
    }

    @Test
    @DisplayName("Deve logar erro quando exception ocorre no CompletableFuture")
    void deveLogarErroQuandoExceptionOcorre() throws Exception {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        CompletableFuture<Boolean> futureComErro = new CompletableFuture<>();
        futureComErro.completeExceptionally(new RuntimeException("Erro de teste"));
        when(emailExecutor.enviarEmailAssincrono(any(EmailDto.class))).thenReturn(futureComErro);

        String para = "recipient@test.com";
        String assunto = "Test";
        String corpo = "Test body";

        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(emailExecutor).enviarEmailAssincrono(any(EmailDto.class));
    }

    @Test
    @DisplayName("Deve capturar RuntimeException durante processamento")
    void deveCapturaRuntimeException() throws Exception {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenThrow(new RuntimeException("Erro de teste"));

        String para = "recipient@test.com";
        String assunto = "Test";
        String corpo = "Test body";

        // Não deve lançar exceção, apenas logar
        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(repositorioNotificacao).save(any(Notificacao.class));
        verify(emailExecutor, never()).enviarEmailAssincrono(any(EmailDto.class));
    }
}
