package sgc.processo.api.eventos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoRevisaoSubprocessoDisponibilizada {
    private Long subprocessoCodigo;
}
