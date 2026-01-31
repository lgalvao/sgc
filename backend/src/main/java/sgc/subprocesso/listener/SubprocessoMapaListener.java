package sgc.subprocesso.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sgc.mapa.eventos.EventoMapaAlterado;
import sgc.subprocesso.service.SubprocessoFacade;

/**
 * Listener assíncrono para eventos do módulo mapa.
 *
 * <p>Responsável por atualizar a situação do subprocesso quando o mapa é alterado,
 * mantendo o desacoplamento entre os módulos via eventos.
 *
 * <p><b>Fase 3 (ADR-002):</b> Tornado assíncrono para desacoplamento completo
 * entre módulos mapa e subprocesso.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SubprocessoMapaListener {

    private final SubprocessoFacade subprocessoFacade;

    /**
     * Processa evento de mapa alterado de forma assíncrona.
     *
     * <p>Atualiza a situação do subprocesso para "Em Andamento" quando o mapa é modificado.
     *
     * @param evento Evento contendo o código do mapa alterado
     */
    @EventListener
    public void handleMapaAlterado(EventoMapaAlterado evento) {
        subprocessoFacade.atualizarSituacaoParaEmAndamento(evento.mapaCodigo());
    }
}
