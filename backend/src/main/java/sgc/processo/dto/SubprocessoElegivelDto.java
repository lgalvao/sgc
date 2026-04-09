package sgc.processo.dto;

import lombok.*;
import org.jspecify.annotations.*;
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
    boolean habilitarAceitarBloco;
    boolean habilitarHomologarBloco;
    boolean habilitarDisponibilizarBloco;
    @Nullable LocalDateTime ultimaDataLimite;
}
