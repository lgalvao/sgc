package sgc.mapa.dto;

import lombok.*;

import java.time.*;
import java.util.*;

@Builder
public record AtualizarEstadoMapaCommand(
        Optional<LocalDateTime> dataHoraDisponibilizado,
        Optional<String> observacoesDisponibilizacao,
        Optional<String> sugestoes,
        Optional<LocalDateTime> dataHoraHomologado
) {
    public AtualizarEstadoMapaCommand {
        dataHoraDisponibilizado = dataHoraDisponibilizado != null ? dataHoraDisponibilizado : Optional.empty();
        observacoesDisponibilizacao = observacoesDisponibilizacao != null ? observacoesDisponibilizacao : Optional.empty();
        sugestoes = sugestoes != null ? sugestoes : Optional.empty();
        dataHoraHomologado = dataHoraHomologado != null ? dataHoraHomologado : Optional.empty();
    }
}
