package sgc.processo.dto;

import lombok.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Value
@Builder
public class SubprocessoElegivelDto {
    Long codigo;
    Long unidadeCodigo;
    String unidadeNome;
    String unidadeSigla;
    Long localizacaoCodigo;
    SituacaoSubprocesso situacao;
    LocalDateTime ultimaDataLimite;
}
