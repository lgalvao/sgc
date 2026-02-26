package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.seguranca.sanitizacao.*;

@Builder
public record CriarConhecimentoRequest(
        @NotNull(message = "Código da atividade é obrigatório")
        Long atividadeCodigo,

        @NotBlank(message = "Descrição não pode ser vazia")
        @SanitizarHtml
        String descricao
) {
}
