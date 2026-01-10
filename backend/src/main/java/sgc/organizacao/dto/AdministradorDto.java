package sgc.organizacao.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdministradorDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private String tituloEleitoral;
    private String nome;
    private String matricula;
    private Long unidadeCodigo;
    private String unidadeSigla;
}
