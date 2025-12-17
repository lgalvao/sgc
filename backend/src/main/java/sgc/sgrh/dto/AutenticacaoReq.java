package sgc.sgrh.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * DTO para a requisição de autenticação de um usuário.
 */
@Value
@Builder
public class AutenticacaoReq {
    @NotNull(message = "O título eleitoral é obrigatório.")
    @Size(max = 20, message = "O título eleitoral deve ter no máximo 20 caracteres.")
    String tituloEleitoral;

    @NotNull(message = "A senha é obrigatória.")
    @Size(max = 128, message = "A senha deve ter no máximo 128 caracteres.")
    String senha;

    @JsonCreator
    public AutenticacaoReq(
            @JsonProperty("tituloEleitoral") String tituloEleitoral,
            @JsonProperty("senha") String senha) {
        this.tituloEleitoral = tituloEleitoral;
        this.senha = senha;
    }
}
