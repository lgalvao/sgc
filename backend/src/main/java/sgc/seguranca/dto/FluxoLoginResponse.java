package sgc.seguranca.dto;

import lombok.*;

import java.util.*;

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
