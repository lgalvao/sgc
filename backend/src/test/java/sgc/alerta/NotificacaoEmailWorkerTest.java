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
    private NotificacaoEmailService notificacaoEmailService;
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
        when(notificacaoEmailService.listarPendentes(20)).thenReturn(List.of(notificacao));

        worker.processarPendentes();

        verify(notificacaoEmailService).marcarEnviando(notificacao);
        verify(emailService).enviarEmailHtml("destino@tre-pe.jus.br", "Assunto", "<p>corpo</p>");
        verify(notificacaoEmailService).marcarEnviado(notificacao);
        verify(notificacaoEmailService, never()).marcarFalha(any(), any());
    }

    @Test
    @DisplayName("processarPendentes deve marcar falha quando SMTP falhar")
    void processarPendentesDeveMarcarFalhaQuandoSmtpFalhar() {
        NotificacaoEmail notificacao = notificacao();
        RuntimeException erro = new RuntimeException("SMTP fora");
        when(notificacaoEmailService.listarPendentes(20)).thenReturn(List.of(notificacao));
        doThrow(erro).when(emailService).enviarEmailHtml("destino@tre-pe.jus.br", "Assunto", "<p>corpo</p>");

        worker.processarPendentes();

        verify(notificacaoEmailService).marcarEnviando(notificacao);
        verify(notificacaoEmailService).marcarFalha(notificacao, erro);
        verify(notificacaoEmailService, never()).marcarEnviado(any());
    }

    private NotificacaoEmail notificacao() {
        return NotificacaoEmail.builder()
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .build();
    }
}
