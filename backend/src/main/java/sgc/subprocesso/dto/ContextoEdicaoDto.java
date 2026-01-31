package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.organizacao.dto.UnidadeDto;

import java.util.List;

/**
 * DTO de resposta contendo o contexto completo para edição de um subprocesso.
 */
@Builder
public record ContextoEdicaoDto(
        UnidadeDto unidade,
        SubprocessoDetalheDto subprocesso,
        @Nullable MapaCompletoDto mapa,
        List<AtividadeDto> atividadesDisponiveis
) {
}
