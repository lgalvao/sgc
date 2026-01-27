package sgc.organizacao.dto;

import lombok.Builder;

/**
 * DTO para representação de administrador.
 */
@Builder
public record AdministradorDto(
        String tituloEleitoral,
        String nome,
        String matricula,
        Long unidadeCodigo,
        String unidadeSigla) {
}
