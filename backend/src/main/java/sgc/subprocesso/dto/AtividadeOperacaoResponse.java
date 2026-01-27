package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

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
                /**
                 * A atividade que foi criada, atualizada ou afetada pela operação.
                 * Pode ser null em caso de exclusão.
                 */
                @Nullable AtividadeVisualizacaoDto atividade,

                /**
                 * Status atualizado do subprocesso após a operação.
                 * Inclui situação, label e timestamp.
                 */
                SubprocessoSituacaoDto subprocesso,

                /**
                 * Lista completa de atividades do subprocesso após a operação.
                 * Permite que o frontend atualize o cache local sem chamadas adicionais.
                 */
                List<AtividadeVisualizacaoDto> atividadesAtualizadas) {
}
