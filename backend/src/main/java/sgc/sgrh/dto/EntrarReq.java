package sgc.sgrh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Value
@Builder
public class EntrarReq {
    @NotNull(message = "O título eleitoral é obrigatório.")
    String tituloEleitoral;

    @NotNull(message = "O perfil é obrigatório.")
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