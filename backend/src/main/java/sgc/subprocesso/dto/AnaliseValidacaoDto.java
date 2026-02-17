package sgc.subprocesso.dto;

import lombok.Builder;

import java.time.LocalDateTime;

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
