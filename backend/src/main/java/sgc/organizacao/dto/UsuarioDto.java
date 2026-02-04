package sgc.organizacao.dto;

import lombok.Builder;
import sgc.comum.validacao.TituloEleitoral;

/**
 * DTO para representação de usuário.
 */
@Builder
public record UsuarioDto(
        @TituloEleitoral String tituloEleitoral,
        String nome,
        String email,
        String matricula,
        Long unidadeCodigo) {
}
