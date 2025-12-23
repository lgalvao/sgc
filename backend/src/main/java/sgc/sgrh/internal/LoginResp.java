package sgc.sgrh.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.sgrh.internal.model.Perfil;

/**
 * DTO para resposta de login.''
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {
    private String tituloEleitoral;
    private String nome;
    private Perfil perfil;
    private Long unidadeCodigo;
    private String token;
}
