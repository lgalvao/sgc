package sgc.alerta;

import org.springframework.stereotype.*;
import sgc.alerta.dto.*;
import sgc.alerta.model.*;

@Component
public class AlertaDtoMapper {

    public AlertaDto paraAlertaDto(Alerta alerta) {
        return AlertaDto.builder()
                .codigo(alerta.getCodigo())
                .codProcesso(alerta.getCodProcessoSintetico())
                .processo(alerta.getProcessoDescricaoSintetica())
                .origem(alerta.getOrigemSiglaSintetica())
                .unidadeDestino(alerta.getUnidadeDestinoSigla())
                .descricao(alerta.getDescricao())
                .mensagem(alerta.getMensagemSintetica())
                .dataHora(alerta.getDataHora())
                .dataHoraLeitura(alerta.getDataHoraLeitura())
                .build();
    }

    public NotificacaoDto paraNotificacaoDto(NotificacaoEmail notificacao) {
        Long subprocessoCodigo = notificacao.getSubprocesso() != null ? notificacao.getSubprocesso().getCodigo() : null;
        String processoDescricao = notificacao.getSubprocesso() != null && notificacao.getSubprocesso().getProcesso() != null
                ? notificacao.getSubprocesso().getProcesso().getDescricao()
                : null;

        return NotificacaoDto.builder()
                .codigo(notificacao.getCodigo())
                .subprocessoCodigo(subprocessoCodigo)
                .unidadeSigla(notificacao.getUnidadeDestinoSigla())
                .processoDescricao(processoDescricao)
                .tipoNotificacao(TipoNotificacaoDto.valueOf(notificacao.getTipoNotificacao().name()))
                .usuarioDestinoTitulo(notificacao.getUsuarioDestinoTitulo())
                .destinatario(notificacao.getDestinatario())
                .assunto(notificacao.getAssunto())
                .corpoHtml(notificacao.getCorpoHtml())
                .situacao(SituacaoNotificacaoEmailDto.valueOf(notificacao.getSituacao().name()))
                .tentativas(notificacao.getTentativas())
                .dataHoraCriacao(notificacao.getDataHoraCriacao())
                .dataHoraEnvio(notificacao.getDataHoraEnvio())
                .proximaTentativaEm(notificacao.getProximaTentativaEm())
                .ultimoErro(notificacao.getUltimoErro())
                .build();
    }
}
