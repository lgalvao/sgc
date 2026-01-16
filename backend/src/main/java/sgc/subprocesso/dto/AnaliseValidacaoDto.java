package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO para histórico de análise de validação (CDU-20 item 6).
 */
@Getter
@Builder
@AllArgsConstructor
public class AnaliseValidacaoDto {

    /**
     * O código da análise.
     */
    private final Long codigo;

    /**
     * A data e hora da análise.
     */
    private final LocalDateTime dataHora;

    /**
     * As observações registradas.
     */
    private final String observacoes;

    /**
     * A ação realizada (e.g., APROVADO, DEVOLVIDO).
     */
    private final String acao;

    /**
     * A sigla da unidade que realizou a análise.
     */
    private final String unidadeSigla;
}
