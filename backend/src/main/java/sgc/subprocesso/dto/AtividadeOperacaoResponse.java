package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * DTO de resposta para operações CRUD em atividades.
 *
 * <p>Retorna tanto a atividade afetada quanto o status atualizado do subprocesso,
 * permitindo que o frontend atualize a UI sem precisar fazer chamadas adicionais
 * para buscar o processo completo.
 */
@Getter
@Builder
@AllArgsConstructor
public class AtividadeOperacaoResponse {
    /**
     * A atividade que foi criada, atualizada ou afetada pela operação.
     * Pode ser null em caso de exclusão.
     */
    @Nullable
    private final AtividadeVisualizacaoDto atividade;

    /**
     * Status atualizado do subprocesso após a operação.
     * Inclui situação, label e timestamp.
     */
    private final SubprocessoSituacaoDto subprocesso;
}
