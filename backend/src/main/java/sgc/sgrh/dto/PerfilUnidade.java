package sgc.sgrh.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
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