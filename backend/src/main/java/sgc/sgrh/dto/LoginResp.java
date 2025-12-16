package sgc.sgrh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.sgrh.model.Perfil;

/**
 * DTO para resposta de login.
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

    // Construtor compatível com chamadas existentes que usavam (String, Perfil, Long)
    public LoginResp(String tituloEleitoral, Perfil perfil, Long unidadeCodigo) {
        this.tituloEleitoral = tituloEleitoral;
        this.perfil = perfil;
        this.unidadeCodigo = unidadeCodigo;
        this.token = null;
    }
}
