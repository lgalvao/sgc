package sgc.processo.dto;

import lombok.Builder;
import sgc.processo.model.SituacaoProcesso;

import java.time.LocalDateTime;

/**
 * DTO de resposta com resumo de um processo.
 */
@Builder
public record ProcessoResumoDto(
        Long codigo,
        String descricao,
        SituacaoProcesso situacao,
        String tipo,
        LocalDateTime dataLimite,
        LocalDateTime dataCriacao,
        Long unidadeCodigo,
        String unidadeNome,
        String unidadesParticipantes,
        String linkDestino
) {
}
