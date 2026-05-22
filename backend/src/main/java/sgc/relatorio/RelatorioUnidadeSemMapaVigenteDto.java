package sgc.relatorio;

import java.util.List;

public record RelatorioUnidadeSemMapaVigenteDto(
        Long codigo,
        String sigla,
        String nome,
        String tipo,
        List<RelatorioUnidadeSemMapaVigenteDto> filhas
) {
}
