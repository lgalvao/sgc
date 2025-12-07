package sgc.sgrh.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

/** DTO para a requisição de autenticação de um usuário. */
@Value
@Builder
public class AutenticacaoReq {
    @NotNull(message = "O título eleitoral é obrigatório.")
    String tituloEleitoral;

    @NotNull(message = "A senha é obrigatória.")
    String senha;

    @JsonCreator
    public AutenticacaoReq(
            @JsonProperty("tituloEleitoral") String tituloEleitoral,
            @JsonProperty("senha") String senha) {
        this.tituloEleitoral = tituloEleitoral;
        this.senha = senha;
    }
}
