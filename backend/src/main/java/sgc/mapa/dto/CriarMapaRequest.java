package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.*;

import java.time.*;
import java.util.*;

@Builder
public record CriarMapaRequest(
        @NotNull(message = Mensagens.CODIGO_SUBPROCESSO_OBRIGATORIO)
        Long subprocessoCodigo,
        @Nullable LocalDateTime dataHoraDisponibilizado,
        @Nullable String observacoesDisponibilizacao,
        @Nullable String sugestoes,
        @Nullable LocalDateTime dataHoraHomologado) {

    public CriarMapaCommand paraCommand() {
        return CriarMapaCommand.builder()
                .subprocessoCodigo(subprocessoCodigo)
                .estadoInicial(AtualizarEstadoMapaCommand.builder()
                        .dataHoraDisponibilizado(Optional.ofNullable(dataHoraDisponibilizado))
                        .observacoesDisponibilizacao(Optional.ofNullable(observacoesDisponibilizacao))
                        .sugestoes(Optional.ofNullable(sugestoes))
                        .dataHoraHomologado(Optional.ofNullable(dataHoraHomologado))
                        .build())
                .build();
    }
}
