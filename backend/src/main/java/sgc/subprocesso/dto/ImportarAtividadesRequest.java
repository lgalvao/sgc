package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

import java.util.*;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 */
@Builder
public record ImportarAtividadesRequest(
        @NotNull(message = SgcMensagens.CODIGO_SUBPROCESSO_ORIGEM_OBRIGATORIO)
        Long codSubprocessoOrigem,
        List<Long> codigosAtividades) {
}
