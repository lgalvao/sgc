package sgc.processo.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.processo.model.*;

import java.time.*;

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
        @Nullable String linkDestino
) {
    public static ProcessoResumoDto fromEntity(Processo processo) {
        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo() != null ? processo.getTipo().name() : null)
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .unidadesParticipantes(processo.getSiglasParticipantes())
                .build();
    }
}
