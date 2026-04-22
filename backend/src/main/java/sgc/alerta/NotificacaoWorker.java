package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sgc.alerta.model.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoWorker {
    private final NotificacaoService notificacaoService;
    private final EmailService emailService;

    @Value("${sgc.notificacao-email.lote-worker:20}")
    private int loteWorker;

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
