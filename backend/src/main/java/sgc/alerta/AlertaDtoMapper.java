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
        String processoDescricao = notificacao.getSubprocesso() != null
                ? notificacao.getSubprocesso().getProcesso().getDescricao()
                : null;
        String unidadeOrigemSigla = inferirUnidadeOrigemSigla(notificacao);

        return NotificacaoDto.builder()
                .codigo(notificacao.getCodigo())
                .subprocessoCodigo(subprocessoCodigo)
                .unidadeSigla(notificacao.getUnidadeDestinoSigla())
                .unidadeOrigemSigla(unidadeOrigemSigla)
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

    private String inferirUnidadeOrigemSigla(NotificacaoEmail notificacao) {
        if (notificacao.getSubprocesso() == null) {
            return "ADMIN";
        }
        return notificacao.getSubprocesso().getUnidade().getSigla();
    }

    public NotificacaoSubprocessoResumoDto paraNotificacaoSubprocessoResumo(NotificacaoSubprocessoResumoQuery query) {
        sgc.alerta.dto.SituacaoNotificacao status = calcularStatusGeral(query);
        return new NotificacaoSubprocessoResumoDto(
                query.subprocessoCodigo(),
                query.processoCodigo(),
                query.processoDescricao(),
                query.unidadeSigla(),
                query.situacaoSubprocesso() != null ? query.situacaoSubprocesso().name() : null,
                query.totalNotificacoes(),
                query.pendentes(),
                query.enviando(),
                query.enviadas(),
                query.falhasTemporarias(),
                query.falhasDefinitivas(),
                status.name(),
                query.ultimaNotificacaoEm(),
                query.proximaTentativaEm(),
                query.maiorTentativas(),
                query.ultimoErro(),
                query.falhasDefinitivas() > 0
        );
    }

    private sgc.alerta.dto.SituacaoNotificacao calcularStatusGeral(NotificacaoSubprocessoResumoQuery query) {
        if (query.falhasDefinitivas() > 0) {
            return sgc.alerta.dto.SituacaoNotificacao.FALHA_DEFINITIVA;
        }
        if (query.falhasTemporarias() > 0) {
            return sgc.alerta.dto.SituacaoNotificacao.FALHA_TEMPORARIA;
        }
        if (query.pendentes() > 0 || query.enviando() > 0) {
            return sgc.alerta.dto.SituacaoNotificacao.PENDENTE;
        }
        if (query.totalNotificacoes() == 0) {
            return sgc.alerta.dto.SituacaoNotificacao.INCONSISTENTE;
        }
        return sgc.alerta.dto.SituacaoNotificacao.OK;
    }
}
