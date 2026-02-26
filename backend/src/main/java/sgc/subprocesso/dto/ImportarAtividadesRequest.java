package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 */
@Builder
public record ImportarAtividadesRequest(
        @NotNull(message = "O código do subprocesso de origem é obrigatório") 
        Long codSubprocessoOrigem) {
}
