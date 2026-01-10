package sgc.processo.dto;

import lombok.Builder;
import lombok.Value;
import sgc.subprocesso.model.SituacaoSubprocesso;

@Value
@Builder
public class SubprocessoElegivelDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    Long codSubprocesso;
    String unidadeNome;
    String unidadeSigla;
    SituacaoSubprocesso situacao;
}
