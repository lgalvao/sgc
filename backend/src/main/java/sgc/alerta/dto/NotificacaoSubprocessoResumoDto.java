package sgc.alerta.dto;

import sgc.subprocesso.model.*;

import java.time.*;

public record NotificacaoSubprocessoResumoDto(
        Long subprocessoCodigo,
        Long processoCodigo,
        String processoDescricao,
        String unidadeSigla,
        SituacaoSubprocesso situacaoSubprocesso,
        long totalNotificacoes,
        long pendentes,
        long enviando,
        long enviadas,
        long falhasTemporarias,
        long falhasDefinitivas,
        SituacaoNotificacao statusGeral,
        LocalDateTime ultimaNotificacaoEm,
        LocalDateTime proximaTentativaEm,
        int maiorTentativas,
        String ultimoErro,
        boolean podeReenviar
) {
    public static NotificacaoSubprocessoResumoDto fromQuery(NotificacaoSubprocessoResumoQuery query) {
        SituacaoNotificacao status = calcularStatus(query);
        return new NotificacaoSubprocessoResumoDto(
                query.subprocessoCodigo(),
                query.processoCodigo(),
                query.processoDescricao(),
                query.unidadeSigla(),
                query.situacaoSubprocesso(),
                query.totalNotificacoes(),
                query.pendentes(),
                query.enviando(),
                query.enviadas(),
                query.falhasTemporarias(),
                query.falhasDefinitivas(),
                status,
                query.ultimaNotificacaoEm(),
                query.proximaTentativaEm(),
                query.maiorTentativas(),
                query.ultimoErro(),
                query.falhasDefinitivas() > 0
        );
    }

    private static SituacaoNotificacao calcularStatus(NotificacaoSubprocessoResumoQuery query) {
        if (query.falhasDefinitivas() > 0) {
            return SituacaoNotificacao.FALHA_DEFINITIVA;
        }
        if (query.falhasTemporarias() > 0) {
            return SituacaoNotificacao.FALHA_TEMPORARIA;
        }
        if (query.pendentes() > 0 || query.enviando() > 0) {
            return SituacaoNotificacao.PENDENTE;
        }
        if (query.totalNotificacoes() == 0) {
            return SituacaoNotificacao.INCONSISTENTE;
        }
        return SituacaoNotificacao.OK;
    }
}
