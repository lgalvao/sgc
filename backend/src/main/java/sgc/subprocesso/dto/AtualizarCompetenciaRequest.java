package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.comum.Mensagens;

import java.util.List;

@Builder
public record AtualizarCompetenciaRequest(
        @NotBlank(message = Mensagens.DESCRICAO_COMPETENCIA_OBRIGATORIA)
        String descricao,

        List<Long> atividadesCodigos) {
}
