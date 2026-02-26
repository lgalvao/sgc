package sgc.seguranca.dto;

import lombok.*;
import sgc.organizacao.model.*;

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
