package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import sgc.comum.Mensagens;

import java.util.List;

@Builder
public record CriarCompetenciaRequest(
        @NotBlank(message = Mensagens.DESCRICAO_COMPETENCIA_OBRIGATORIA)
        String descricao,

        @NotEmpty(message = Mensagens.COMPETENCIA_DEVE_TER_ATIVIDADE)
        List<Long> atividadesCodigos) {
}
