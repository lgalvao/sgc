package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SubprocessoPermissoesDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final boolean podeVerPagina;
    private final boolean podeEditarMapa;
    private final boolean podeVisualizarMapa;
    private final boolean podeDisponibilizarMapa;
    private final boolean podeDisponibilizarCadastro;
    private final boolean podeDevolverCadastro;
    private final boolean podeAceitarCadastro;
    private final boolean podeVisualizarDiagnostico;
    private final boolean podeAlterarDataLimite;
    private final boolean podeVisualizarImpacto;
    private final boolean podeRealizarAutoavaliacao;
    private final boolean podeReabrirCadastro;
    private final boolean podeReabrirRevisao;
    private final boolean podeEnviarLembrete;
}
