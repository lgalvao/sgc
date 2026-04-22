package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;
import sgc.alerta.model.*;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoEmailWorker")
@SuppressWarnings("NullAway.Init")
class NotificacaoEmailWorkerTest {
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificacaoEmailWorker worker;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(worker, "loteWorker", 20);
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
