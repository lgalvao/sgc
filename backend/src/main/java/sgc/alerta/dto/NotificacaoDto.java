package sgc.alerta.dto;

import lombok.*;
import sgc.alerta.model.*;

import java.time.*;

@Builder
public record NotificacaoDto(
        Long codigo,
        Long subprocessoCodigo,
        String unidadeSigla,
        String processoDescricao,
        TipoNotificacao tipoNotificacao,
        String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        String corpoHtml,
        sgc.alerta.model.SituacaoNotificacao situacao,
        int tentativas,
        LocalDateTime dataHoraCriacao,
        LocalDateTime dataHoraEnvio,
        LocalDateTime proximaTentativaEm,
        String ultimoErro
) {
    public static NotificacaoDto fromEntity(NotificacaoEmail notificacao) {
        Long subId = notificacao.getSubprocesso() != null ? notificacao.getSubprocesso().getCodigo() : null;
        String desc = (notificacao.getSubprocesso() != null && notificacao.getSubprocesso().getProcesso() != null)
                ? notificacao.getSubprocesso().getProcesso().getDescricao()
                : null;

        return NotificacaoDto.builder()
                .codigo(notificacao.getCodigo())
                .subprocessoCodigo(subId)
                .unidadeSigla(notificacao.getUnidadeDestinoSigla())
                .processoDescricao(desc)
                .tipoNotificacao(notificacao.getTipoNotificacao())
                .usuarioDestinoTitulo(notificacao.getUsuarioDestinoTitulo())
                .destinatario(notificacao.getDestinatario())
                .assunto(notificacao.getAssunto())
                .corpoHtml(notificacao.getCorpoHtml())
                .situacao(notificacao.getSituacao())
                .tentativas(notificacao.getTentativas())
                .dataHoraCriacao(notificacao.getDataHoraCriacao())
                .dataHoraEnvio(notificacao.getDataHoraEnvio())
                .proximaTentativaEm(notificacao.getProximaTentativaEm())
                .ultimoErro(notificacao.getUltimoErro())
                .build();
    }
}
