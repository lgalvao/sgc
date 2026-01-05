package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaService;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;

/**
 * Listener responsável por processar eventos de transição de subprocesso
 * para fins de comunicação (alertas internos e e-mails).
 *
 * <p>Este listener é disparado quando o {@link SubprocessoTransicaoService} publica
 * um {@link EventoTransicaoSubprocesso}. Ele processa o evento e:
 * <ul>
 *   <li>Cria alertas internos via {@link AlertaService} (se aplicável ao tipo)</li>
 *   <li>Envia e-mails de notificação via {@link SubprocessoEmailService}</li>
 * </ul>
 *
 * <p>A decisão sobre criar ou não alerta/e-mail é baseada nos metadados do
 * {@link TipoTransicao} (campos {@code templateAlerta} e {@code templateEmail}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubprocessoComunicacaoListener {

    private final AlertaService alertaService;
    private final SubprocessoEmailService emailService;

    /**
     * Processa eventos de transição de subprocesso para criar alertas e enviar e-mails.
     *
     * @param evento Evento de transição publicado pelo SubprocessoTransicaoService
     */
    @EventListener
    @Transactional
    public void handle(EventoTransicaoSubprocesso evento) {
        Subprocesso sp = evento.getSubprocesso();
        TipoTransicao tipo = evento.getTipo();

        // 1. Criar alerta (se aplicável)
        if (tipo.geraAlerta()) {
            criarAlerta(sp, evento);
        }

        // 2. Enviar e-mail (se aplicável)
        if (tipo.enviaEmail()) {
            emailService.enviarEmailTransicao(evento);
        }
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
