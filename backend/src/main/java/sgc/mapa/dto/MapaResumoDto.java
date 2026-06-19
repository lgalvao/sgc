package sgc.mapa.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MapaResumoDto(
        Long codigo,
        Long subprocessoCodigo,
        LocalDateTime dataHoraDisponibilizado,
        String observacoesDisponibilizacao,
        String sugestoes,
        LocalDateTime dataHoraHomologado) {
}
