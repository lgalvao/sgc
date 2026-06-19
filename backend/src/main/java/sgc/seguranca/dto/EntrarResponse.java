package sgc.seguranca.dto;

import lombok.Builder;

/**
 * DTO para resposta de login.
 */
@Builder
public record EntrarResponse(
        String tituloEleitoral,
        String nome,
        String perfil,
        Long unidadeCodigo,
        PermissoesSessaoResponse permissoes) {
}
