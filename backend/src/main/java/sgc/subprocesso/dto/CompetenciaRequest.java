package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

import java.util.*;

/**
 * DTO de requisição para adicionar/atualizar competência.
 */
@Builder
public record CompetenciaRequest(
        @NotBlank(message = Mensagens.DESCRICAO_COMPETENCIA_OBRIGATORIA)
        String descricao,

        List<Long> atividadesIds) {
}
