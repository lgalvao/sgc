package sgc.mapa.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

@Builder
public record AtualizarEstadoMapaCommand(
        @Nullable LocalDateTime dataHoraDisponibilizado,
        @Nullable String observacoesDisponibilizacao,
        @Nullable String sugestoes,
        @Nullable LocalDateTime dataHoraHomologado) {
}
