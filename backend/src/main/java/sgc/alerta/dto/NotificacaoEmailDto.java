package sgc.alerta.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.alerta.model.*;

import java.time.*;

@Builder
public record NotificacaoEmailDto(
        Long codigo,
        @Nullable Long alertaCodigo,
        @Nullable Long subprocessoCodigo,
        @Nullable String tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        SituacaoNotificacaoEmail situacao,
        int tentativas,
        LocalDateTime dataHoraCriacao,
        @Nullable LocalDateTime dataHoraEnvio,
        @Nullable LocalDateTime proximaTentativaEm,
        @Nullable String ultimoErro
) {
    public static NotificacaoEmailDto fromEntity(NotificacaoEmail notificacao, Long subprocessoCodigo) {
        Alerta alerta = notificacao.getAlerta();
        return NotificacaoEmailDto.builder()
                .codigo(notificacao.getCodigo())
                .alertaCodigo(alerta == null ? null : alerta.getCodigo())
                .subprocessoCodigo(subprocessoCodigo)
                .tipoNotificacao(notificacao.getTipoNotificacao())
                .usuarioDestinoTitulo(notificacao.getUsuarioDestinoTitulo())
                .destinatario(notificacao.getDestinatario())
                .assunto(notificacao.getAssunto())
                .situacao(notificacao.getSituacao())
                .tentativas(notificacao.getTentativas())
                .dataHoraCriacao(notificacao.getDataHoraCriacao())
                .dataHoraEnvio(notificacao.getDataHoraEnvio())
                .proximaTentativaEm(notificacao.getProximaTentativaEm())
                .ultimoErro(notificacao.getUltimoErro())
                .build();
    }
}
