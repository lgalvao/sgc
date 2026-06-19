package sgc.mapa.dto;

import java.util.List;

public record MapaCompletoDto(
        Long codigo,
        Long subprocessoCodigo,
        String observacoes,
        List<CompetenciaMapaDto> competencias,
        List<AtividadeMapaDto> atividades,
        String situacao) {
}
