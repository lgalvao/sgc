package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.comum.Mensagens;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para atualização de Conhecimento.
 */
@Builder
public record AtualizarConhecimentoRequest(
        @NotBlank(message = Mensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
