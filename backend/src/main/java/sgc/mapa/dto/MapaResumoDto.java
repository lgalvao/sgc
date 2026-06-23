package sgc.mapa.dto;

import lombok.*;

import java.time.*;

@Builder
public record MapaResumoDto(
        Long codigo,
        Long subprocessoCodigo,
        LocalDateTime dataHoraDisponibilizado,
        String observacoesDisponibilizacao,
        String sugestoes,
        LocalDateTime dataHoraHomologado) {
}
