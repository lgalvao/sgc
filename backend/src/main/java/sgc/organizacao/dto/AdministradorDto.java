package sgc.organizacao.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdministradorDto {

    private final String tituloEleitoral;
    private final String nome;
    private final String matricula;
    private final Long unidadeCodigo;
    private final String unidadeSigla;
}
