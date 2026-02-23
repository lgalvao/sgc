package sgc.seguranca.dto;

import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;

/**
 * DTO que representa um par perfil/unidade para autorização.
 */
public record PerfilUnidadeDto(
        Perfil perfil,
        UnidadeDto unidade) {
}
