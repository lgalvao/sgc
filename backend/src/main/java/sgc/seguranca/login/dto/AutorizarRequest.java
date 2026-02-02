package sgc.seguranca.login.dto;

import lombok.Builder;
import sgc.comum.validacao.TituloEleitoral;

/**
 * DTO para a requisição de autorização de um usuário.
 */
@Builder
public record AutorizarRequest(
        @TituloEleitoral String tituloEleitoral) {
}
