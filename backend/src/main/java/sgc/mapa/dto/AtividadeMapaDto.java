package sgc.mapa.dto;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record AtividadeMapaDto(
        @Nullable Long codigo,
        String descricao,
        List<ConhecimentoResumoDto> conhecimentos) {
}
