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

@Component
@RequiredArgsConstructor
@Slf4j
public class SubprocessoComunicacaoListener {
    private final AlertaService alertaService;
    private final SubprocessoEmailService emailService;

    @EventListener
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
