package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.SgcMensagens;

import java.util.*;

/**
 * DTO de requisição para adicionar/atualizar competência.
 */
@Builder
public record CompetenciaRequest(
        @NotBlank(message = SgcMensagens.DESCRICAO_COMPETENCIA_OBRIGATORIA)
        String descricao,

        @NotEmpty(message = SgcMensagens.COMPETENCIA_DEVE_TER_ATIVIDADE)
        List<Long> atividadesIds) {
}
