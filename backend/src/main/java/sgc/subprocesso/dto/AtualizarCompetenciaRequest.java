package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

import java.util.*;

@Builder
public record AtualizarCompetenciaRequest(
        @NotBlank(message = Mensagens.DESCRICAO_COMPETENCIA_OBRIGATORIA)
        String descricao,

        List<Long> atividadesCodigos) {
}
