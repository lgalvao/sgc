package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.seguranca.sanitizacao.*;

import java.util.*;

@Builder
public record CriarCompetenciaRequest(
        @NotBlank(message = Mensagens.DESCRICAO_COMPETENCIA_OBRIGATORIA)
        @SanitizarHtml
        String descricao,

        @NotEmpty(message = Mensagens.COMPETENCIA_DEVE_TER_ATIVIDADE)
        List<Long> atividadesCodigos) {
}
