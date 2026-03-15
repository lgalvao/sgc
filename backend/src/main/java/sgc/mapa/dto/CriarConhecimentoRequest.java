package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;
import sgc.seguranca.sanitizacao.*;

@Builder
public record CriarConhecimentoRequest(
        @NotNull(message = MsgValidacao.CODIGO_ATIVIDADE_OBRIGATORIO)
        Long atividadeCodigo,

        @NotBlank(message = MsgValidacao.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
