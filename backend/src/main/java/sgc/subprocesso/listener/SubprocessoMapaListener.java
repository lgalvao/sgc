package sgc.subprocesso.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.subprocesso.service.SubprocessoFacade;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubprocessoMapaListener {

    private final SubprocessoFacade subprocessoFacade;

    @EventListener
    public void handleMapaAlterado(EventoMapaAlterado evento) {
        subprocessoFacade.atualizarSituacaoParaEmAndamento(evento.mapaCodigo());
    }
}
