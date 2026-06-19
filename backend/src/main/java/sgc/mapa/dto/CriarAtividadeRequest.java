package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sgc.comum.Mensagens;
import sgc.seguranca.sanitizacao.SanitizarHtml;

@Builder
public record CriarAtividadeRequest(
        @NotNull(message = Mensagens.CODIGO_MAPA_OBRIGATORIO)
        Long mapaCodigo,

        @NotBlank(message = Mensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
