package sgc.subprocesso.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO para histórico de análise de validação (CDU-20 item 6).
 */
@Builder
public record AnaliseValidacaoDto(
                /**
                 * O código da análise.
                 */
                Long codigo,

                /**
                 * A data e hora da análise.
                 */
                LocalDateTime dataHora,

                /**
                 * As observações registradas.
                 */
                String observacoes,

                /**
                 * A ação realizada (e.g., APROVADO, DEVOLVIDO).
                 */
                String acao,

                /**
                 * A sigla da unidade que realizou a análise.
                 */
                String unidadeSigla) {
}
