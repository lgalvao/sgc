package sgc.mapa.dto;

import org.jspecify.annotations.*;

import java.util.*;

public record MapaCompletoDto(
        Long codigo,
        Long subprocessoCodigo,
        @Nullable String observacoes,
        List<CompetenciaMapaDto> competencias,
        List<AtividadeMapaDto> atividades,
        @Nullable String situacao) {
}
