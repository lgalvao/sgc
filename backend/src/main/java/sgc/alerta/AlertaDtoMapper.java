package sgc.alerta;

import org.springframework.stereotype.*;
import sgc.alerta.dto.*;
import sgc.alerta.model.*;
import sgc.processo.model.*;

@Component
public class AlertaDtoMapper {
    private static final String DESCRICAO_PROCESSO_FINALIZADO = "Processo finalizado";
    private static final String DESCRICAO_PROCESSO_FINALIZADO_SUBORDINADAS = "Processo finalizado em unidades subordinadas";

    public AlertaDto paraAlertaDto(Alerta alerta) {
        boolean processoFinalizado = alerta.getProcesso() != null
                && alerta.getProcesso().getSituacao() == SituacaoProcesso.FINALIZADO;
        boolean alertaFinalizacaoProcesso = DESCRICAO_PROCESSO_FINALIZADO.equals(alerta.getDescricao())
                || DESCRICAO_PROCESSO_FINALIZADO_SUBORDINADAS.equals(alerta.getDescricao());

        return AlertaDto.builder()
                .codigo(alerta.getCodigo())
                .codProcesso(alerta.getCodProcessoSintetico())
                .processo(alerta.getProcessoDescricaoSintetica())
                .origem(alerta.getOrigemSiglaSintetica())
                .unidadeDestino(alerta.getUnidadeDestinoSigla())
                .descricao(alerta.getDescricao())
                .mensagem(alerta.getMensagemSintetica())
                .processoFinalizado(processoFinalizado)
                .alertaFinalizacaoProcesso(alertaFinalizacaoProcesso)
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
        boolean processoFinalizado = notificacao.getSubprocesso() != null
                && notificacao.getSubprocesso().getProcesso().getSituacao() == SituacaoProcesso.FINALIZADO;
        boolean notificacaoFinalizacaoProcesso = notificacao.getTipoNotificacao() == TipoNotificacao.PROCESSO_FINALIZADO;

        return NotificacaoDto.builder()
                .codigo(notificacao.getCodigo())
                .subprocessoCodigo(subprocessoCodigo)
                .unidadeSigla(notificacao.getUnidadeDestinoSigla())
                .unidadeOrigemSigla(unidadeOrigemSigla)
                .processoDescricao(processoDescricao)
                .processoFinalizado(processoFinalizado)
                .tipoNotificacao(TipoNotificacaoDto.valueOf(notificacao.getTipoNotificacao().name()))
                .notificacaoFinalizacaoProcesso(notificacaoFinalizacaoProcesso)
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
