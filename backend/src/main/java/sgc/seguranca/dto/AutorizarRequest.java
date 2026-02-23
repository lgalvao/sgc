package sgc.seguranca.dto;

import lombok.Builder;
import sgc.comum.TituloEleitoral;

/**
 * DTO para a requisição de autorização de um usuário.
 */
@Builder
public record AutorizarRequest(
        @TituloEleitoral String tituloEleitoral) {
}
