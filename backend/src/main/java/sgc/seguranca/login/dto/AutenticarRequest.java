package sgc.seguranca.login.dto;

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
public class AutenticarRequest {
    @NotNull(message = "O título é obrigatório.")
    @Size(max = 12, message = "O título deve ter no máximo 12 caracteres.")
    String tituloEleitoral;

    @NotNull(message = "A senha é obrigatória.")
    @Size(max = 64, message = "A senha deve ter no máximo 64 caracteres.")
    String senha;

    @JsonCreator
    public AutenticarRequest(
            @JsonProperty("tituloEleitoral") String tituloEleitoral,
            @JsonProperty("senha") String senha) {
        this.tituloEleitoral = tituloEleitoral;
        this.senha = senha;
    }
}
