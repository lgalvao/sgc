package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 */
@Builder
public record ImportarAtividadesRequest(
        @NotNull(message = "O código do subprocesso de origem é obrigatório") 
        Long codSubprocessoOrigem) {
}
