package sgc.subprocesso.eventos;

import sgc.subprocesso.model.SituacaoSubprocesso;

/**
 * Evento unificado para transições de situação de subprocesso.
 * Segue o padrão definido em ADR-002.
 */
public record EventoTransicaoSubprocesso(
    Long codigoSubprocesso,
    SituacaoSubprocesso novaSituacao,
    TipoTransicao tipoTransicao,
    String tituloEleitoral
) {}
