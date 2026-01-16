package sgc.subprocesso.service.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;

/**
 * Listener assíncrono para eventos de transição de subprocesso.
 *
 * <p>Responsável por processar comunicações (alertas e e-mails) de forma assíncrona,
 * desacoplando essas operações da transação principal do workflow.
 *
 * <p><b>Fase 3 (ADR-002):</b> Tornado assíncrono para desacoplamento completo entre
 * workflow principal e comunicação/notificação.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubprocessoComunicacaoListener {
    private final AlertaFacade alertaService;
    private final SubprocessoEmailService emailService;

    /**
     * Processa evento de transição de forma assíncrona.
     *
     * <p>Este método é executado em uma thread separada da transação principal,
     * permitindo que falhas na comunicação não afetem o workflow do subprocesso.
     *
     * @param evento Evento de transição contendo dados do subprocesso e da transição
     */
    @EventListener
    @Async
    @Transactional
    public void handle(EventoTransicaoSubprocesso evento) {
        Subprocesso sp = evento.getSubprocesso();
        TipoTransicao tipo = evento.getTipo();

        if (tipo.geraAlerta()) criarAlerta(sp, evento);
        if (tipo.enviaEmail()) emailService.enviarEmailTransicao(evento);
    }

    private void criarAlerta(Subprocesso sp, EventoTransicaoSubprocesso evento) {
        TipoTransicao tipo = evento.getTipo();
        String descricao = tipo.formatarAlerta(sp.getUnidade().getSigla());

        alertaService.criarAlertaTransicao(
            sp.getProcesso(), 
            descricao, 
            evento.getUnidadeOrigem(), 
            evento.getUnidadeDestino()
        );
    }
}
