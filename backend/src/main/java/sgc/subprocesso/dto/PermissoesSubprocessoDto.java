package sgc.subprocesso.dto;

import lombok.*;

/**
 * DTO que consolida as permissões de UI para um subprocesso,
 * calculado com base no perfil do usuário e localização do subprocesso
 */
@Builder
public record PermissoesSubprocessoDto(
        boolean podeEditarCadastro,
        boolean podeDisponibilizarCadastro,
        boolean podeDevolverCadastro,
        boolean podeAceitarCadastro,
        boolean podeHomologarCadastro,
        boolean podeEditarMapa,
        boolean podeDisponibilizarMapa,
        boolean podeValidarMapa,
        boolean podeApresentarSugestoes,
        boolean podeVerSugestoes,
        boolean podeDevolverMapa,
        boolean podeAceitarMapa,
        boolean podeHomologarMapa,
        boolean podeVisualizarImpacto,
        boolean podeAlterarDataLimite,
        boolean podeReabrirCadastro,
        boolean podeReabrirRevisao,
        boolean podeEnviarLembrete,
        boolean habilitarAcessoCadastro,
        boolean habilitarAcessoMapa
) {
}
