package sgc.sgrh.api;

import lombok.*;
import sgc.sgrh.internal.model.Perfil;

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
