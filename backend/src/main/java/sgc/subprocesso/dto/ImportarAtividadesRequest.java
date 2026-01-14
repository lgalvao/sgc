package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 * CDU-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportarAtividadesRequest {
    /**
     * O código do subprocesso do qual as atividades serão importadas.
     */
    @NotNull(message = "O código do subprocesso de origem é obrigatório")
    private Long codSubprocessoOrigem;
}
