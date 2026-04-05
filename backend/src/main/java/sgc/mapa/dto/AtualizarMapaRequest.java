package sgc.mapa.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;
@Builder
public record AtualizarMapaRequest(
        @Nullable LocalDateTime dataHoraDisponibilizado,
        @Nullable String observacoesDisponibilizacao,
        @Nullable String sugestoes,
        @Nullable LocalDateTime dataHoraHomologado) {

    public AtualizarEstadoMapaCommand paraCommand() {
        return AtualizarEstadoMapaCommand.builder()
                .dataHoraDisponibilizado(dataHoraDisponibilizado)
                .observacoesDisponibilizacao(observacoesDisponibilizacao)
                .sugestoes(sugestoes)
                .dataHoraHomologado(dataHoraHomologado)
                .build();
    }
}
