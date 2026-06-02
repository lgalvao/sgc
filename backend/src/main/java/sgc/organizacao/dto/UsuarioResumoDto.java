package sgc.organizacao.dto;

import lombok.*;
import org.jspecify.annotations.*;

@Builder
public record UsuarioResumoDto(
        String tituloEleitoral,
        String matricula,
        String nome,
        String email,
        String ramal
) {
}
