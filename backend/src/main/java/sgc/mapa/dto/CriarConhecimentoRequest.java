package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.SgcMensagens;
import sgc.seguranca.sanitizacao.*;

@Builder
public record CriarConhecimentoRequest(
        @NotNull(message = SgcMensagens.CODIGO_ATIVIDADE_OBRIGATORIO)
        Long atividadeCodigo,

        @NotBlank(message = SgcMensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
