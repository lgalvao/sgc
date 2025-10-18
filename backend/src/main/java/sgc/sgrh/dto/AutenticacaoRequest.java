package sgc.sgrh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@AllArgsConstructor
@Jacksonized
public class AutenticacaoRequest {
    @NotNull(message = "O título eleitoral é obrigatório.")
    Long tituloEleitoral;
    @NotNull(message = "A senha é obrigatória.")
    String senha;
}