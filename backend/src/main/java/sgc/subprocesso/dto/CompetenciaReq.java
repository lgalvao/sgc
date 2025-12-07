package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
