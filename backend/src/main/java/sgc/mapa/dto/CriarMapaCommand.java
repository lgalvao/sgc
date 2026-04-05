package sgc.mapa.dto;

import lombok.*;

@Builder
public record CriarMapaCommand(
        Long subprocessoCodigo,
        AtualizarEstadoMapaCommand estadoInicial
) {
    public CriarMapaCommand {
        estadoInicial = estadoInicial != null ? estadoInicial : AtualizarEstadoMapaCommand.builder().build();
    }
}
