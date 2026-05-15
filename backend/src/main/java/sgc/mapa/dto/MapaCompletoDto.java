package sgc.mapa.dto;

import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

public record MapaCompletoDto(
        Long codigo,
        Long subprocessoCodigo,
        String observacoes,
        List<CompetenciaMapaDto> competencias,
        List<AtividadeMapaDto> atividades,
        String situacao) {

    public static MapaCompletoDto fromEntity(Mapa mapa) {
        Subprocesso subprocesso = mapa.getSubprocesso();
        List<CompetenciaMapaDto> competencias = mapa.getCompetencias().stream()
                .map(CompetenciaMapaDto::fromEntity)
                .toList();

        List<AtividadeMapaDto> atividades = mapa.getAtividades().stream()
                .map(AtividadeMapaDto::fromEntity)
                .toList();

        return new MapaCompletoDto(
                mapa.getCodigo(),
                subprocesso.getCodigo(),
                mapa.getObservacoesDisponibilizacao(),
                competencias,
                atividades,
                null);
    }
}
