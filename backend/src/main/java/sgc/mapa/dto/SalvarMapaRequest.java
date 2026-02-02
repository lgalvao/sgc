package sgc.mapa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.util.List;

/**
 * DTO de requisição para salvar mapa. CDU-09 item 1 a 4.
 */
@Builder
public record SalvarMapaRequest(
        @Nullable @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres") @SanitizarHtml String observacoes,
        @Valid List<CompetenciaMapaDto> competencias) {
}
