package sgc.processo.dto;

import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

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
