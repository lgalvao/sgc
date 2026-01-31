package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.mapa.dto.visualizacao.AtividadeDto;

import java.util.List;

/**
 * DTO de resposta para operações CRUD em atividades.
 *
 * <p>
 * Retorna tanto a atividade afetada quanto o status atualizado do subprocesso,
 * permitindo que o frontend atualize a UI sem precisar fazer chamadas
 * adicionais
 * para buscar o processo completo.
 */
@Builder
public record AtividadeOperacaoResponse(
        @Nullable AtividadeDto atividade,

                SubprocessoSituacaoDto subprocesso,

                List<AtividadeDto> atividadesAtualizadas) {
}
