package sgc.seguranca.login.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * DTO para a requisição de entrada (login) do usuário.
 */
@Value
@Builder
public class EntrarReq {
    @NotNull(message = "O título eleitoral é obrigatório.")
    @Size(max = 20, message = "O título eleitoral deve ter no máximo 20 caracteres.")
    String tituloEleitoral;

    @NotNull(message = "O perfil é obrigatório.")
    @Size(max = 50, message = "O perfil deve ter no máximo 50 caracteres.")
    String perfil;

    @NotNull(message = "O código da unidade é obrigatório.")
    Long unidadeCodigo;

    @JsonCreator
    public EntrarReq(
            @JsonProperty("tituloEleitoral") String tituloEleitoral,
            @JsonProperty("perfil") String perfil,
            @JsonProperty("unidadeCodigo") Long unidadeCodigo) {
        this.tituloEleitoral = tituloEleitoral;
        this.perfil = perfil;
        this.unidadeCodigo = unidadeCodigo;
    }
}
