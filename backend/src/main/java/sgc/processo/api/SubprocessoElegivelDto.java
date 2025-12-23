package sgc.processo.api;

import lombok.Builder;
import lombok.Value;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;

@Value
@Builder
public class SubprocessoElegivelDto {
    Long codSubprocesso;
    String unidadeNome;
    String unidadeSigla;
    SituacaoSubprocesso situacao;
}
