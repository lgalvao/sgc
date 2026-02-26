package sgc.subprocesso.dto;

import lombok.*;

import java.time.*;

/**
 * DTO para histórico de análise de validação (CDU-20 item 6).
 */
@Builder
public record AnaliseValidacaoDto(
        Long codigo,
        LocalDateTime dataHora,
        String observacoes,
        String acao,
        String unidadeSigla) {
}
