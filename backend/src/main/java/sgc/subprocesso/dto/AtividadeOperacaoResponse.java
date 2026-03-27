package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.mapa.dto.*;

import java.util.*;

/**
 * DTO de resposta para operações CRUD em atividades.
 * <p>
 * Retorna a atividade afetada e o status atualizado do subprocesso, permitindo
 * que o frontend atualize a UI sem precisar fazer chamadas adicionais para
 * buscar o processo completo.
 */
@Builder
public record AtividadeOperacaoResponse(
        @Nullable AtividadeDto atividade,
        SubprocessoSituacaoDto subprocesso,
        List<AtividadeDto> atividadesAtualizadas,
        PermissoesSubprocessoDto permissoes) {
}
