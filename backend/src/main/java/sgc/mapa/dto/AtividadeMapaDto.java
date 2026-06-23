package sgc.mapa.dto;

import org.jspecify.annotations.*;

import java.util.*;

public record AtividadeMapaDto(
        @Nullable Long codigo,
        String descricao,
        List<ConhecimentoResumoDto> conhecimentos) {
}
