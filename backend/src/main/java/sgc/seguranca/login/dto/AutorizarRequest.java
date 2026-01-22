package sgc.seguranca.login.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * DTO para a requisição de autorização de um usuário.
 */
@Value
@Builder
public class AutorizarRequest {
    @NotNull(message = "O título eleitoral é obrigatório.")
    @Size(max = 12, message = "O título eleitoral deve ter no máximo 12 caracteres.")
    @Pattern(regexp = "^\\d+$", message = "O título eleitoral deve conter apenas números.")
    String tituloEleitoral;

    @JsonCreator
    public AutorizarRequest(
            @JsonProperty("tituloEleitoral") String tituloEleitoral) {
        this.tituloEleitoral = tituloEleitoral;
    }
}
