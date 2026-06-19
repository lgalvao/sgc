package sgc.seguranca.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO da resposta inicial do fluxo de login.
 */
@Builder
public record FluxoLoginResponse(
        boolean autenticado,
        boolean requerSelecaoPerfil,
        List<PerfilUnidadeDto> perfisUnidades,
        EntrarResponse sessao) {
}
