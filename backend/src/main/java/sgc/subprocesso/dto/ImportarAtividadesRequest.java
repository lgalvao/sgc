package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 */
@Builder
public record ImportarAtividadesRequest(
        @NotNull(message = "O código do subprocesso de origem é obrigatório")
        Long codSubprocessoOrigem,
        List<Long> codigosAtividades) {
}
