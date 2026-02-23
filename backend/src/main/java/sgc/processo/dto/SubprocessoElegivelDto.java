package sgc.processo.dto;

import lombok.Builder;
import lombok.Value;
import sgc.subprocesso.model.SituacaoSubprocesso;

@Value
@Builder
public class SubprocessoElegivelDto {
    Long codigo;
    Long unidadeCodigo;
    String unidadeNome;
    String unidadeSigla;
    SituacaoSubprocesso situacao;
}
