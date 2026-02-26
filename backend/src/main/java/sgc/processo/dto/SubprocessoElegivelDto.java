package sgc.processo.dto;

import lombok.*;
import sgc.subprocesso.model.*;

@Value
@Builder
public class SubprocessoElegivelDto {
    Long codigo;
    Long unidadeCodigo;
    String unidadeNome;
    String unidadeSigla;
    SituacaoSubprocesso situacao;
}
