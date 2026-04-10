package sgc.processo.painel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.alerta.dto.AlertaDto;
import sgc.processo.dto.ProcessoResumoDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PainelBootstrapDto {
    private List<ProcessoResumoDto> processos;
    private List<AlertaDto> alertas;
}
