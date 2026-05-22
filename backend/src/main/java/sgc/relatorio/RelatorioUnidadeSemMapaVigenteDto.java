package sgc.relatorio;

import java.util.*;

public record RelatorioUnidadeSemMapaVigenteDto(
        Long codigo,
        String sigla,
        String nome,
        String tipo,
        List<RelatorioUnidadeSemMapaVigenteDto> filhas
) {
}
