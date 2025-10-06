package sgc.subprocesso.dto;

import java.time.LocalDateTime;

/**
 * DTO para histórico de análise de validação (CDU-20 item 6).
 */
public record AnaliseValidacaoDTO(
    Long id,
    LocalDateTime dataHora,
    String observacoes
) {}