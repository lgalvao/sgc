package sgc.alerta.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.TipoNotificacao;

import java.time.*;

@Builder
public record NotificacaoDto(
        Long codigo,
        @Nullable Long subprocessoCodigo,
        @Nullable TipoNotificacao tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        sgc.alerta.model.SituacaoNotificacao situacao,
        int tentativas,
        LocalDateTime dataHoraCriacao,
        @Nullable LocalDateTime dataHoraEnvio,
        @Nullable LocalDateTime proximaTentativaEm,
        @Nullable String ultimoErro
) {
    public static NotificacaoDto fromEntity(NotificacaoEmail notificacao, Long subprocessoCodigo) {
        return NotificacaoDto.builder()
                .codigo(notificacao.getCodigo())
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
