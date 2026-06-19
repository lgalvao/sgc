package sgc.alerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.model.NotificacaoEmail;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoWorker")
@SuppressWarnings("NullAway.Init")
class NotificacaoWorkerTest {
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private EmailService emailService;

    private NotificacaoWorker worker;

    @BeforeEach
    void setUp() {
        worker = new NotificacaoWorker(notificacaoService, emailService, 20);
    }

    @Test
    @DisplayName("processarPendentes deve enviar e marcar sucesso")
    void processarPendentesDeveEnviarEMarcarSucesso() {
        NotificacaoEmail notificacao = notificacao();
        when(notificacaoService.listarPendentes(20)).thenReturn(List.of(notificacao));
        when(notificacaoService.marcarEnviandoSeDisponivel(notificacao)).thenReturn(true);

        worker.processarPendentes();

        verify(notificacaoService).marcarEnviandoSeDisponivel(notificacao);
        verify(emailService).enviarEmailHtml("destino@tre-pe.jus.br", "Assunto", "<p>corpo</p>");
        verify(notificacaoService).marcarEnviado(notificacao);
        verify(notificacaoService, never()).marcarFalha(any(), any());
    }

    @Test
    @DisplayName("processarPendentes deve marcar falha quando SMTP falhar")
    void processarPendentesDeveMarcarFalhaQuandoSmtpFalhar() {
        NotificacaoEmail notificacao = notificacao();
        RuntimeException erro = new RuntimeException("SMTP fora");
        when(notificacaoService.listarPendentes(20)).thenReturn(List.of(notificacao));
        when(notificacaoService.marcarEnviandoSeDisponivel(notificacao)).thenReturn(true);
        doThrow(erro).when(emailService).enviarEmailHtml("destino@tre-pe.jus.br", "Assunto", "<p>corpo</p>");

        worker.processarPendentes();

        verify(notificacaoService).marcarEnviandoSeDisponivel(notificacao);
        verify(notificacaoService).marcarFalha(notificacao, erro);
        verify(notificacaoService, never()).marcarEnviado(any());
    }

    @Test
    @DisplayName("processarPendentes deve ignorar notificacao ja capturada por outro worker")
    void processarPendentesDeveIgnorarNotificacaoJaCapturadaPorOutroWorker() {
        NotificacaoEmail notificacao = notificacao();
        when(notificacaoService.listarPendentes(20)).thenReturn(List.of(notificacao));
        when(notificacaoService.marcarEnviandoSeDisponivel(notificacao)).thenReturn(false);

        worker.processarPendentes();

        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
        verify(notificacaoService, never()).marcarEnviado(any());
        verify(notificacaoService, never()).marcarFalha(any(), any());
    }

    private NotificacaoEmail notificacao() {
        return NotificacaoEmail.builder()
                .codigo(10L)
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .build();
    }
}
