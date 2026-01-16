package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO de requisição para adicionar/atualizar competência.
 */
@Getter
@Builder
@AllArgsConstructor
public class CompetenciaRequest {
    @NotBlank(message = "A descrição da competência é obrigatória")
    private final String descricao;

    @NotEmpty(message = "A competência deve ter pelo menos uma atividade associada")
    private final List<Long> atividadesIds;
}
