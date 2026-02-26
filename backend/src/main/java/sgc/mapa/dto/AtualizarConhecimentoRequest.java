package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.seguranca.sanitizacao.*;

/**
 * Request para atualização de Conhecimento.
 */
@Builder
public record AtualizarConhecimentoRequest(
        @NotBlank(message = "Descrição não pode ser vazia")
        @SanitizarHtml
        String descricao
) {
}
