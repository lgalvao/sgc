package sgc.unidade.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUnidadeDto {
    private String perfil;
    private Long unidadeCodigo;
    private String sigla;
}