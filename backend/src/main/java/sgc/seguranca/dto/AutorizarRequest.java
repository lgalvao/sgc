package sgc.seguranca.dto;

import lombok.*;
import sgc.comum.model.*;

/**
 * DTO para a requisição de autorização de um usuário.
 */
@Builder
public record AutorizarRequest(
        @TituloEleitoral String tituloEleitoral) {
}
