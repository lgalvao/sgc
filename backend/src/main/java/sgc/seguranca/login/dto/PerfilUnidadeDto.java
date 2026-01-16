package sgc.seguranca.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;

/**
 * DTO que representa um par perfil/unidade para autorização.
 */
@Getter
@Builder
@AllArgsConstructor
public class PerfilUnidadeDto {

    private final Perfil perfil;
    private final UnidadeDto unidade;
}
