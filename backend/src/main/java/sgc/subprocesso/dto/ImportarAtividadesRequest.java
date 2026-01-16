package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 * CDU-08
 */
@Getter
@Builder
@AllArgsConstructor
public class ImportarAtividadesRequest {
    /**
     * O código do subprocesso do qual as atividades serão importadas.
     */
    @NotNull(message = "O código do subprocesso de origem é obrigatório")
    private final Long codSubprocessoOrigem;
}
