package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO de requisição para adicionar/atualizar competência.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaRequest {
    @NotBlank(message = "A descrição da competência é obrigatória")
    private String descricao;

    @NotEmpty(message = "A competência deve ter pelo menos uma atividade associada")
    private List<Long> atividadesIds;
}
