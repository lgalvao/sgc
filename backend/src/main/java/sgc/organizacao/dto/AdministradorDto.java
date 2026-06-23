package sgc.organizacao.dto;

import lombok.*;
import sgc.comum.model.*;

/**
 * DTO para representação de administrador.
 */
@Builder
public record AdministradorDto(
        @TituloEleitoral
        String tituloEleitoral,

        String nome,
        String matricula,
        Long unidadeCodigo,
        String unidadeSigla) {
}
