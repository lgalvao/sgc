package sgc.unidade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUnidadeDTO {
    private String perfil;
    private Long unidadeCodigo;
    private String sigla;
}