package sgc.analise.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

@Builder
public record CriarAnaliseRequest(
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @SanitizarHtml
    String observacoes,

    @Size(max = 20, message = "Sigla da unidade deve ter no máximo 20 caracteres")
    String siglaUnidade,

    @Size(max = 12, message = "Título do usuário deve ter no máximo 12 caracteres")
    String tituloUsuario,

    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    @SanitizarHtml
    String motivo
) {}
