package sgc.alerta.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.alerta.model.*;

import java.time.*;

@Builder
public record NotificacaoEmailDto(
        Long codigo,
        Long subprocessoCodigo,
        @Nullable String tipoTransicao,
        String destinatario,
        String assunto,
        SituacaoNotificacaoEmail situacao,
        int tentativas,
        LocalDateTime dataHoraCriacao,
        @Nullable LocalDateTime dataHoraEnvio,
        @Nullable LocalDateTime proximaTentativaEm
) {
    public static NotificacaoEmailDto fromEntity(NotificacaoEmail notificacao, Long subprocessoCodigo) {
        return NotificacaoEmailDto.builder()
                .codigo(notificacao.getCodigo())
                .subprocessoCodigo(subprocessoCodigo)
                .tipoTransicao(notificacao.getTipoTransicao())
                .destinatario(notificacao.getDestinatario())
                .assunto(notificacao.getAssunto())
                .situacao(notificacao.getSituacao())
                .tentativas(notificacao.getTentativas())
                .dataHoraCriacao(notificacao.getDataHoraCriacao())
                .dataHoraEnvio(notificacao.getDataHoraEnvio())
                .proximaTentativaEm(notificacao.getProximaTentativaEm())
                .build();
    }
}
