package sgc.usuario.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdministradorDto {
    private String tituloEleitoral;
    private String nome;
    private String matricula;
    private Long unidadeCodigo;
    private String unidadeSigla;
}
