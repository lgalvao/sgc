package sgc.sgrh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.sgrh.model.Perfil;

/**
 * DTO para resposta de login.''
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {
    private String tituloEleitoral;
    private Perfil perfil;
    private Long unidadeCodigo;
    private String token;
}
