package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para atualização de Conhecimento.
 */
@Builder
public record AtualizarConhecimentoRequest(
    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizarHtml
    String descricao
) {}
