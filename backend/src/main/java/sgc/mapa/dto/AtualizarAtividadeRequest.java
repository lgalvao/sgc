package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para atualização de Atividade.
 */
@Builder
public record AtualizarAtividadeRequest(
        @NotBlank(message = "Descrição não pode ser vazia")
        @SanitizarHtml
        String descricao
) {
}
