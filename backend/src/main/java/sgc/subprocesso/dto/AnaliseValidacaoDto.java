package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para histórico de análise de validação (CDU-20 item 6).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseValidacaoDto {
    /** O código da análise. */
    private Long codigo;
    /** A data e hora da análise. */
    private LocalDateTime dataHora;
    /** As observações registradas. */
    private String observacoes;
    /** A ação realizada (e.g., APROVADO, DEVOLVIDO). */
    private String acao;
    /** A sigla da unidade que realizou a análise. */
    private String unidadeSigla;
}
