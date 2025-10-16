package sgc.sgrh.dto;

import lombok.*;
import sgc.sgrh.Perfil;
import sgc.unidade.modelo.Unidade;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PerfilUnidade {

    private Perfil perfil;
    private Unidade unidade;

}