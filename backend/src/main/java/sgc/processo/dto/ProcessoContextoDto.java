package sgc.processo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProcessoContextoDto {

    private final ProcessoDetalheDto processo;
    private final List<SubprocessoElegivelDto> elegiveis;
}
