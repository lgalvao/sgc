package sgc.sgrh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.sgrh.modelo.Perfil;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {
    private Long tituloEleitoral;
    private Perfil perfil;
    private Long unidadeCodigo;
    private String token;

    public LoginResp(Long tituloEleitoral, Perfil perfil, Long unidadeCodigo) {
        this.tituloEleitoral = tituloEleitoral;
        this.perfil = perfil;
        this.unidadeCodigo = unidadeCodigo;
        this.token = null;
    }
}
