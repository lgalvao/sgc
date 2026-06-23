package sgc.subprocesso.dto;

import lombok.*;
import sgc.mapa.dto.*;
import sgc.organizacao.dto.*;

/**
 * Resposta agregada para o contexto de edição de um subprocesso.
 */
@Builder
public record ContextoEdicaoResponse(
        UnidadeResumoDto unidade,
        SubprocessoResumoDto subprocesso,
        SubprocessoDetalheResponse detalhes,
        MapaCompletoDto mapa
) {
}
