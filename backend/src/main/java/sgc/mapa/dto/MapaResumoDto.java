package sgc.mapa.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.mapa.model.*;

import java.time.*;

@Builder
public record MapaResumoDto(
        Long codigo,
        @Nullable Long subprocessoCodigo,
        @Nullable LocalDateTime dataHoraDisponibilizado,
        @Nullable String observacoesDisponibilizacao,
        @Nullable String sugestoes,
        @Nullable LocalDateTime dataHoraHomologado) {

    public static MapaResumoDto fromEntity(Mapa mapa) {
        return MapaResumoDto.builder()
                .codigo(mapa.getCodigo())
                .subprocessoCodigo(mapa.getSubprocesso() != null ? mapa.getSubprocesso().getCodigo() : null)
                .dataHoraDisponibilizado(mapa.getDataHoraDisponibilizado())
                .observacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao())
                .sugestoes(mapa.getSugestoes())
                .dataHoraHomologado(mapa.getDataHoraHomologado())
                .build();
    }
}
