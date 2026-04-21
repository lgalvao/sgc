package sgc.alerta;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import sgc.alerta.model.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoEmailWorker {
    private final NotificacaoEmailService notificacaoEmailService;
    private final EmailService emailService;

    @Value("${sgc.notificacao-email.lote-worker:20}")
    private int loteWorker;

    @Scheduled(fixedDelayString = "${sgc.notificacao-email.intervalo-worker-ms:30000}")
    public void processarPendentes() {
        for (NotificacaoEmail notificacao : notificacaoEmailService.listarPendentes(loteWorker)) {
            processar(notificacao);
        }
    }

    private void processar(NotificacaoEmail notificacao) {
        notificacaoEmailService.marcarEnviando(notificacao);
        try {
            emailService.enviarEmailHtml(
                    notificacao.getDestinatario(),
                    notificacao.getAssunto(),
                    notificacao.getCorpoHtml()
            );
            notificacaoEmailService.marcarEnviado(notificacao);
        } catch (Exception ex) {
            log.warn("Falha ao enviar notificacao de email {}", notificacao.getCodigo(), ex);
            notificacaoEmailService.marcarFalha(notificacao, ex);
        }
    }
}
