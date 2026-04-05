package sgc.mapa.dto;

import lombok.*;
import org.jspecify.annotations.*;

@Builder
public record CriarMapaCommand(
        Long subprocessoCodigo,
        @Nullable AtualizarEstadoMapaCommand estadoInicial
) {
}
