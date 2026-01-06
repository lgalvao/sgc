package sgc.seguranca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.organizacao.model.Perfil;

/**
 * DTO para resposta de login.''
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrarResp {
    private String tituloEleitoral;
    private String nome;
    private Perfil perfil;
    private Long unidadeCodigo;
    private String token;
}
