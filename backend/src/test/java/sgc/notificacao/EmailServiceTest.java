package sgc.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import sgc.comum.config.ConfigAplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    private static final int LIMITE_CONTEUDO_NOTIFICACAO = 500;
    private static final int TAMANHO_CORPO_LONGO = 600;

    @Mock
    private NotificacaoRepo repositorioNotificacao;

    @Mock
    private JavaMailSender enviadorDeEmail;

    @Mock
    private ConfigAplicacao config;

    @InjectMocks
    private EmailService notificacaoServico;

    private static final String RECIPIENT = "recipient@test.com";
    private static final String TEST_BODY = "Test body";

    private void setupMockEmail() {
        ConfigAplicacao.Email emailConfig = new ConfigAplicacao.Email();
        emailConfig.setRemetente("noreply@test.com");
        emailConfig.setRemetenteNome("Remetente Teste");
        emailConfig.setAssuntoPrefixo("[Teste]");
        when(config.getEmail()).thenReturn(emailConfig);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(enviadorDeEmail.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar e-mail HTML")
    void enviarEmailHtmlDeveEnviarComSucesso() {
        setupMockEmail();
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        String para = RECIPIENT;
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        verify(repositorioNotificacao, times(1)).save(any(Notificacao.class));
        verify(enviadorDeEmail, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço inválido")
    void enviarEmailHtmlNaoDeveEnviarParaEnderecoInvalido() {
        String para = "invalid-email";
        String assunto = "Test Subject";
        String corpoHtml = "<h1>Test Body</h1>";

        notificacaoServico.enviarEmailHtml(para, assunto, corpoHtml);

        verify(enviadorDeEmail, never()).send(any(MimeMessage.class));
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve enviar e-mail de texto simples")
    void deveEnviarEmailTextoSimples() {
        setupMockEmail();
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        String para = RECIPIENT;
        String assunto = "Test Subject Plain";
        String corpo = "This is plain text";

        notificacaoServico.enviarEmail(para, assunto, corpo);
        
        verify(repositorioNotificacao, times(1)).save(any(Notificacao.class));
        verify(enviadorDeEmail, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail para endereço vazio")
    void naoDeveEnviarEmailParaEnderecoVazio() {
        String para = "";
        String assunto = "Test";
        String corpo = "Body";

        notificacaoServico.enviarEmail(para, assunto, corpo);

        verify(enviadorDeEmail, never()).send(any(MimeMessage.class));
        verify(repositorioNotificacao, never()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve truncar conteúdo longo da notificação")
    void deveTruncarConteudoLongoDaNotificacao() {
        setupMockEmail();
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        String assunto = "Test";
        String corpoLongo = "A".repeat(TAMANHO_CORPO_LONGO);

        notificacaoServico.enviarEmail(RECIPIENT, assunto, corpoLongo);

        ArgumentCaptor<Notificacao> captorNotificacao = ArgumentCaptor.forClass(Notificacao.class);
        verify(repositorioNotificacao).save(captorNotificacao.capture());

        Notificacao notificacaoSalva = captorNotificacao.getValue();
        assertThat(notificacaoSalva.getConteudo().length()).isLessThanOrEqualTo(LIMITE_CONTEUDO_NOTIFICACAO);
        assertThat(notificacaoSalva.getConteudo()).endsWith("...");
    }

    @Test
    @DisplayName("Deve logar erro mas não quebrar quando save falha")
    void deveLogarErroQuandoSaveFalha() {
        when(repositorioNotificacao.save(any(Notificacao.class))).thenThrow(new RuntimeException("Erro db"));

        notificacaoServico.enviarEmail(RECIPIENT, "Test", TEST_BODY);

        verify(repositorioNotificacao).save(any(Notificacao.class));
        verify(enviadorDeEmail, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve logar erro quando disparar email falha")
    void deveLogarErroQunadoDisparoFalha() {
        setupMockEmail();
        when(repositorioNotificacao.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Erro email")).when(enviadorDeEmail).send(any(MimeMessage.class));

        notificacaoServico.enviarEmail(RECIPIENT, "Test", TEST_BODY);

        verify(repositorioNotificacao).save(any(Notificacao.class));
        verify(enviadorDeEmail).send(any(MimeMessage.class));
    }
}
