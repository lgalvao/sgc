package sgc.mapa.dto;

import lombok.*;
import sgc.mapa.model.*;

import java.time.*;

@Builder
public record MapaResumoDto(
        Long codigo,
        Long subprocessoCodigo,
        LocalDateTime dataHoraDisponibilizado,
        String observacoesDisponibilizacao,
        String sugestoes,
        LocalDateTime dataHoraHomologado) {

    public static MapaResumoDto fromEntity(Mapa mapa) {
        return MapaResumoDto.builder()
                .codigo(mapa.getCodigo())
                .subprocessoCodigo(mapa.getSubprocesso().getCodigo())
                .dataHoraDisponibilizado(mapa.getDataHoraDisponibilizado())
                .observacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao())
                .sugestoes(mapa.getSugestoes())
                .dataHoraHomologado(mapa.getDataHoraHomologado())
                .build();
    }
}
