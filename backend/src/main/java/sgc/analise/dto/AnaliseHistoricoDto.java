package sgc.analise.dto;

import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.analise.modelo.TipoAnalise;

import java.time.LocalDateTime;

/**
 * DTO para exibir o histórico de uma análise.
 *
 * @param dataHora A data e hora da análise.
 * @param observacoes As observações registradas.
 * @param acao A ação realizada.
 * @param unidadeSigla A sigla da unidade que realizou a análise.
 * @param analistaUsuarioTitulo O título de eleitor do analista.
 * @param motivo O motivo da análise (e.g., para devoluções).
 * @param tipo O tipo de análise (e.g., CADASTRO, VALIDACAO).
 */
// TODO converter para classe com @Builder
public record AnaliseHistoricoDto(
    LocalDateTime dataHora,
    String observacoes,
    TipoAcaoAnalise acao,
    String unidadeSigla,
    String analistaUsuarioTitulo,
    String motivo,
    TipoAnalise tipo
) {}
