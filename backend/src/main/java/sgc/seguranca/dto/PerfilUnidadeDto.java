package sgc.seguranca.dto;

import lombok.*;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PerfilUnidadeDto {
    private Perfil perfil;
    private UnidadeDto unidade;

    public String getSiglaUnidade() {
        return unidade.getSigla();
    }
}
