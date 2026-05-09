package sgc.alerta;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import sgc.alerta.model.*;

@Component
@Slf4j
public class NotificacaoWorker {
    private final NotificacaoService notificacaoService;
    private final EmailService emailService;
    private final int loteWorker;

    public NotificacaoWorker(
            NotificacaoService notificacaoService,
            EmailService emailService,
            @Value("${sgc.notificacao-email.lote-worker:20}") int loteWorker) {
        this.notificacaoService = notificacaoService;
        this.emailService = emailService;
        this.loteWorker = loteWorker;
    }

    @Scheduled(fixedDelayString = "${sgc.notificacao-email.intervalo-worker-ms:30000}")
    public void processarPendentes() {
        for (NotificacaoEmail notificacao : notificacaoService.listarPendentes(loteWorker)) {
            processar(notificacao);
        }
    }

    private void processar(NotificacaoEmail notificacao) {
        if (!notificacaoService.marcarEnviandoSeDisponivel(notificacao)) {
            return;
        }
        try {
            emailService.enviarEmailHtml(
                    notificacao.getDestinatario(),
                    notificacao.getAssunto(),
                    notificacao.getCorpoHtml()
            );
            notificacaoService.marcarEnviado(notificacao);
        } catch (Exception ex) {
            log.warn("Falha ao enviar notificacao de email {}", notificacao.getCodigo(), ex);
            notificacaoService.marcarFalha(notificacao, ex);
        }
    }
}
