package sgc.organizacao.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * DTO para representação de usuário.
 */
@Builder
public record UsuarioDto(
        String tituloEleitoral,
        String nome,
        @Nullable String email,
        @Nullable String matricula,
        @Nullable Long unidadeCodigo) {
}
