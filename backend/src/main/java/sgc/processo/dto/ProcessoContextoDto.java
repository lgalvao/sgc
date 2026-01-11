package sgc.processo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProcessoContextoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final ProcessoDetalheDto processo;
    private final List<SubprocessoElegivelDto> elegiveis;
}
