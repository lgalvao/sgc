package sgc.mapa.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

@Builder
public record CriarMapaCommand(
        Long subprocessoCodigo,
        @Nullable AtualizarEstadoMapaCommand estadoInicial
) {
}
