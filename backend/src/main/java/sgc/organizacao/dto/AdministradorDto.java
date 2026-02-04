package sgc.organizacao.dto;

import lombok.Builder;
import sgc.comum.validacao.TituloEleitoral;

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
