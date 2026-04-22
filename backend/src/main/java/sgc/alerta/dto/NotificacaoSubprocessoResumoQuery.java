package sgc.alerta.dto;

import sgc.subprocesso.model.*;

import java.time.*;

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
