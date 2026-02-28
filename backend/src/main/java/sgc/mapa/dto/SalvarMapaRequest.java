package sgc.mapa.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.seguranca.sanitizacao.*;

import java.util.*;

/**
 * DTO de requisição para salvar mapa. CDU-09 item 1 a 4.
 */
@Builder
public record SalvarMapaRequest(
        @Nullable @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres") @SanitizarHtml String observacoes,
        @Valid List<CompetenciaRequest> competencias) {

    @Builder
    public record CompetenciaRequest(
            Long codigo,

            @NotBlank(message = "A descrição da competência é obrigatória") String descricao,
            List<Long> atividadesCodigos
    ) {
    }
}
