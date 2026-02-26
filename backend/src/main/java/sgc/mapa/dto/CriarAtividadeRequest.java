package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.seguranca.sanitizacao.*;

@Builder
public record CriarAtividadeRequest(
        @NotNull(message = "Código do mapa é obrigatório")
        Long mapaCodigo,

        @NotBlank(message = "Descrição não pode ser vazia")
        @SanitizarHtml
        String descricao
) {
}
