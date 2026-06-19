package sgc.subprocesso.dto;

import lombok.Builder;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.organizacao.dto.UnidadeResumoDto;

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
