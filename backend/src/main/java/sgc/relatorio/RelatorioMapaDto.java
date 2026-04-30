package sgc.relatorio;

import java.util.List;

public record RelatorioMapaDto(
        Long codigoUnidade,
        String siglaUnidade,
        String nomeUnidade,
        int totalCompetencias,
        List<RelatorioMapaCompetenciaDto> competencias
) {
}
