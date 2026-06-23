package sgc.processo.painel.dto;

import lombok.*;
import sgc.alerta.dto.*;
import sgc.processo.dto.*;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PainelBootstrapDto {
    private List<ProcessoResumoDto> processos;
    private List<AlertaDto> alertas;
}
