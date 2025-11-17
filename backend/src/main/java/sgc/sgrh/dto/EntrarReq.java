package sgc.sgrh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@AllArgsConstructor
@Jacksonized
public class EntrarReq {
    @NotNull(message = "O título eleitoral é obrigatório.")
    String tituloEleitoral;

    @NotNull(message = "O perfil é obrigatório.")
    String perfil;

    @NotNull(message = "O código da unidade é obrigatório.")
    Long unidadeCodigo;
}