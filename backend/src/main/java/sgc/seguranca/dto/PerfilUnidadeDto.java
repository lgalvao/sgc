package sgc.seguranca.dto;

import sgc.organizacao.dto.*;

/**
 * DTO que representa um par perfil/unidade para autorização.
 */
public record PerfilUnidadeDto(
        String perfil,
        UnidadeResumoDto unidade) {
}
