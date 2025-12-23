package sgc.subprocesso.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaReq {
    @NotBlank(message = "A descrição da competência é obrigatória")
    private String descricao;

    @NotEmpty(message = "A competência deve ter pelo menos uma atividade associada")
    private List<Long> atividadesIds;
}
