package sgc.mapa.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record CriarMapaCommand(
        Long subprocessoCodigo,
        @Nullable AtualizarEstadoMapaCommand estadoInicial) {
}
