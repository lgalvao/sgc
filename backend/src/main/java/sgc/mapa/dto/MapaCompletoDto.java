package sgc.mapa.dto;

import org.jspecify.annotations.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

public record MapaCompletoDto(
        Long codigo,
        Long subprocessoCodigo,
        @Nullable String observacoes,
        List<CompetenciaMapaDto> competencias,
        @Nullable String situacao) {

    public static MapaCompletoDto fromEntity(Mapa mapa) {
        Subprocesso subprocesso = mapa.getSubprocesso();
        List<CompetenciaMapaDto> competencias = mapa.getCompetencias().stream()
                .map(CompetenciaMapaDto::fromEntity)
                .toList();

        return new MapaCompletoDto(
                mapa.getCodigo(),
                subprocesso != null ? subprocesso.getCodigo() : null,
                mapa.getObservacoesDisponibilizacao(),
                competencias,
                null);
    }
}
