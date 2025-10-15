package sgc.sgrh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AutenticacaoRequest {
    @NotNull(message = "O título eleitoral é obrigatório.")
    Long tituloEleitoral;
    @NotNull(message = "A senha é obrigatória.")
    String senha;
}