package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.seguranca.sanitizacao.*;

@Builder
public record CriarConhecimentoRequest(
        @NotNull(message = Mensagens.CODIGO_ATIVIDADE_OBRIGATORIO)
        Long atividadeCodigo,

        @NotBlank(message = Mensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
