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
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Perfil perfil;
    private UnidadeDto unidade;
}
