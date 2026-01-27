package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

/**
 * DTO de requisição para adicionar/atualizar competência.
 */
@Builder
public record CompetenciaRequest(
                @NotBlank(message = "A descrição da competência é obrigatória") String descricao,

                @NotEmpty(message = "A competência deve ter pelo menos uma atividade associada") List<Long> atividadesIds) {
}
