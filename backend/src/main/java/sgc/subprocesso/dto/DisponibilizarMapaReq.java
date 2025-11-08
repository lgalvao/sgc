package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import sgc.comum.json.SanitizeHtml;

import java.time.LocalDateTime;

/**
 * DTO para requisição de disponibilização do mapa de competências (CDU-17).
 *
 * @param observacoes        Observações adicionais (opcional).
 * @param dataLimiteEtapa2   Prazo para a etapa de validação.
 */
public record DisponibilizarMapaReq(
        @SanitizeHtml
        String observacoes,  // Opcional

        @NotNull(message = "Data limite para validação é obrigatória")
        LocalDateTime dataLimiteEtapa2  // Obrigatório - prazo para validação
) {
}