package sgc.alerta.dto;

import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

public record NotificacaoSubprocessoResumoQuery(
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
        LocalDateTime ultimaNotificacaoEm,
        LocalDateTime proximaTentativaEm,
        int maiorTentativas,
        String ultimoErro
) {
}
