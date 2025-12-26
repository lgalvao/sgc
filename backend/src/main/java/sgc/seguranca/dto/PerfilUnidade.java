package sgc.seguranca.dto;

import lombok.*;
import sgc.usuario.model.Perfil;
import sgc.unidade.dto.UnidadeDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PerfilUnidade {
    private Perfil perfil;
    private UnidadeDto unidade;

    public String getSiglaUnidade() {
        return unidade.getSigla();
    }
}
