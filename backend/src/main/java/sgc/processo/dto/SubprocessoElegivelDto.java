package sgc.processo.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

@Value
@Builder
public class SubprocessoElegivelDto {
    Long codigo;
    Long unidadeCodigo;
    String unidadeNome;
    String unidadeSigla;
    Long localizacaoCodigo;
    String situacao;
    boolean habilitarAceitarCadastroBloco;
    boolean habilitarAceitarMapaBloco;
    boolean habilitarAceitarDiagnosticoBloco;
    boolean habilitarHomologarCadastroBloco;
    boolean habilitarHomologarMapaBloco;
    boolean habilitarDisponibilizarMapaBloco;
    @Nullable LocalDateTime ultimaDataLimite;
}
