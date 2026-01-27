package sgc.organizacao.dto;

import lombok.Builder;
import sgc.organizacao.model.Perfil;

/**
 * DTO para representação de perfil e unidade associada.
 */
@Builder
public record PerfilUnidadeDto(
        Long codigo,
        Perfil perfil,
        String perfilLabel,
        UnidadeDto unidade) {
}
