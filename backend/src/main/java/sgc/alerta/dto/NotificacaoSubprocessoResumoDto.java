package sgc.alerta.dto;

import java.time.*;

public record NotificacaoSubprocessoResumoDto(
        Long subprocessoCodigo,
        Long processoCodigo,
        String processoDescricao,
        String unidadeSigla,
        String situacaoSubprocesso,
        long totalNotificacoes,
        long pendentes,
        long enviando,
        long enviadas,
        long falhasTemporarias,
        long falhasDefinitivas,
        String statusGeral,
        LocalDateTime ultimaNotificacaoEm,
        LocalDateTime proximaTentativaEm,
        int maiorTentativas,
        String ultimoErro,
        boolean podeReenviar
) {
}
