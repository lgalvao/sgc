package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.eventos.EventoImportacaoAtividades;

/**
 * Listener responsável por processar eventos de importação de atividades entre mapas.
 *
 * <p>Este componente escuta {@link EventoImportacaoAtividades} e delega a operação concreta
 * para {@link CopiaMapaService}, eliminando a dependência direta do módulo subprocesso no módulo mapa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MapaImportacaoListener {
    private final CopiaMapaService copiaMapaService;

    /**
     * Processa evento de importação de atividades de forma assíncrona.
     *
     * @param evento dados do evento contendo códigos dos mapas origem e destino
     */
    @EventListener
    @Async
    @Transactional
    public void aoImportarAtividades(EventoImportacaoAtividades evento) {
        log.info("Processando importação de atividades: mapa {} -> mapa {} (subprocesso {})",
                evento.getCodigoMapaOrigem(),
                evento.getCodigoMapaDestino(),
                evento.getCodigoSubprocesso());

        copiaMapaService.importarAtividadesDeOutroMapa(
                evento.getCodigoMapaOrigem(),
                evento.getCodigoMapaDestino());

        log.info("Importação de atividades concluída com sucesso para subprocesso {}",
                evento.getCodigoSubprocesso());
    }
}
