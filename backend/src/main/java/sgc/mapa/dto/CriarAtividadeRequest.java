package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para criação de Atividade.
 */
@Builder
public record CriarAtividadeRequest(
        @NotNull(message = "Código do mapa é obrigatório")
        Long mapaCodigo,

        @NotBlank(message = "Descrição não pode ser vazia")
        @SanitizarHtml
        String descricao
) {
}
