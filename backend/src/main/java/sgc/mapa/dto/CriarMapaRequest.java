package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.*;

import java.time.*;
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
                        .dataHoraDisponibilizado(dataHoraDisponibilizado)
                        .observacoesDisponibilizacao(observacoesDisponibilizacao)
                        .sugestoes(sugestoes)
                        .dataHoraHomologado(dataHoraHomologado)
                        .build())
                .build();
    }
}
