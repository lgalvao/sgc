package sgc.seguranca.login.dto;

import lombok.*;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;

/**
 * DTO que representa um par perfil/unidade para autorização.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PerfilUnidadeDto {

    private Perfil perfil;
    private UnidadeDto unidade;
}
