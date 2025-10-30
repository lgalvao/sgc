package sgc.subprocesso.dto;

import java.time.LocalDateTime;

/**
 * DTO para histórico de análise de validação (CDU-20 item 6).
 *
 * @param codigo O código da análise.
 * @param dataHora A data e hora da análise.
 * @param observacoes As observações registradas.
 * @param acao A ação realizada (e.g., APROVADO, DEVOLVIDO).
 * @param unidadeSigla A sigla da unidade que realizou a análise.
 */
public record AnaliseValidacaoDto(
    Long codigo,
    LocalDateTime dataHora,
    String observacoes,
    String acao,
    String unidadeSigla
) {}