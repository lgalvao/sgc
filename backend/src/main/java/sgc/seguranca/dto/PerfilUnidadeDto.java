package sgc.seguranca.dto;

import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

/**
 * DTO que representa um par perfil/unidade para autorização.
 */
public record PerfilUnidadeDto(
        Perfil perfil,
        UnidadeDto unidade) {
}
