package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.comum.Mensagens;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para atualização de Atividade.
 */
@Builder
public record AtualizarAtividadeRequest(
        @NotBlank(message = Mensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
