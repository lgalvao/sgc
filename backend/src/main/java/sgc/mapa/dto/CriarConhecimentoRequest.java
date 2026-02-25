package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

@Builder
public record CriarConhecimentoRequest(
        @NotNull(message = "Código da atividade é obrigatório")
        Long atividadeCodigo,

        @NotBlank(message = "Descrição não pode ser vazia")
        @SanitizarHtml
        String descricao
) {
}
