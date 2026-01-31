package sgc.mapa.eventos;

import lombok.Builder;
import lombok.Getter;

/**
 * Evento de domínio publicado quando atividades precisam ser importadas de um mapa para outro.
 *
 * <p>Este evento desacopla o módulo {@code subprocesso} do módulo {@code mapa}, permitindo que
 * a operação de importação seja executada de forma assíncrona sem dependências circulares.
 */
@Getter
@Builder
public class EventoImportacaoAtividades {
    /**
     * Código do mapa de origem das atividades.
     */
    private final Long codigoMapaOrigem;

    /**
     * Código do mapa de destino das atividades.
     */
    private final Long codigoMapaDestino;

    /**
     * Código do subprocesso que solicitou a importação.
     */
    private final Long codigoSubprocesso;
}
