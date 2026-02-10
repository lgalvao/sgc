package sgc.subprocesso.dto;

import lombok.Builder;

/**
 * DTO de resposta contendo as permissões do usuário sobre um subprocesso.
 */
@Builder
public record SubprocessoPermissoesDto(
        boolean podeVerPagina,
        boolean podeEditarMapa,
        boolean podeEditarCadastro,
        boolean podeVisualizarMapa,
        boolean podeDisponibilizarMapa,
        boolean podeDisponibilizarCadastro,
        boolean podeDevolverCadastro,
        boolean podeAceitarCadastro,
        boolean podeHomologarCadastro,
        boolean podeVisualizarDiagnostico,
        boolean podeAlterarDataLimite,
        boolean podeVisualizarImpacto,
        boolean podeRealizarAutoavaliacao,
        boolean podeReabrirCadastro,
        boolean podeReabrirRevisao,
        boolean podeEnviarLembrete,
        boolean podeApresentarSugestoes,
        boolean podeValidarMapa,
        boolean podeAceitarMapa,
        boolean podeDevolverMapa,
        boolean podeHomologarMapa
) {
}
