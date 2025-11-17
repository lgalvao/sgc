package sgc.sgrh.dto;

import lombok.*;
import sgc.sgrh.model.Perfil;

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