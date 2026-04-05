package sgc.mapa.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;
import java.util.*;

@Builder
public record AtualizarMapaRequest(
        @Nullable LocalDateTime dataHoraDisponibilizado,
        @Nullable String observacoesDisponibilizacao,
        @Nullable String sugestoes,
        @Nullable LocalDateTime dataHoraHomologado) {

    public AtualizarEstadoMapaCommand paraCommand() {
        return AtualizarEstadoMapaCommand.builder()
                .dataHoraDisponibilizado(Optional.ofNullable(dataHoraDisponibilizado))
                .observacoesDisponibilizacao(Optional.ofNullable(observacoesDisponibilizacao))
                .sugestoes(Optional.ofNullable(sugestoes))
                .dataHoraHomologado(Optional.ofNullable(dataHoraHomologado))
                .build();
    }
}
