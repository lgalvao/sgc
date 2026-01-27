package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 * CDU-08
 */
@Builder
public record ImportarAtividadesRequest(
                /**
                 * O código do subprocesso do qual as atividades serão importadas.
                 */
                @NotNull(message = "O código do subprocesso de origem é obrigatório") Long codSubprocessoOrigem) {
}
