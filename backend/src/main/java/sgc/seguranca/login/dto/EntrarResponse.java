package sgc.seguranca.login.dto;

import lombok.Builder;
import sgc.organizacao.model.Perfil;

/**
 * DTO para resposta de login.
 */
@Builder
public record EntrarResponse(
                String tituloEleitoral,
                String nome,
                Perfil perfil,
                Long unidadeCodigo,
                String token) {
}
