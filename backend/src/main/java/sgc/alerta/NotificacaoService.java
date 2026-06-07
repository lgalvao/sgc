package sgc.alerta;

import lombok.*;
import org.springframework.dao.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.dto.*;
import sgc.alerta.model.*;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.comum.erros.*;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificacaoService {
    private static final int LIMITE_ERRO = 2000;
    private static final int MAX_TENTATIVAS = 5;
    private static final int LIMITE_CONSULTA_MAXIMO = 100;

    private final NotificacaoEmailRepo notificacaoEmailRepo;
    private final Clock clock;
    private final AlertaDtoMapper alertaDtoMapper;

    public NotificacaoEmail enfileirar(EnfileirarNotificacaoCommand cmd) {
        if (notificacaoEmailRepo.existsByChaveIdempotencia(cmd.chaveIdempotencia())) {
            return notificacaoEmailRepo.findByChaveIdempotencia(cmd.chaveIdempotencia())
                    .orElseThrow(() -> new ErroInconsistenciaInterna(
                            "Notificação idempotente ausente para chave %s".formatted(cmd.chaveIdempotencia())));
        }

        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .subprocesso(cmd.subprocesso())
                .tipoNotificacao(cmd.tipoNotificacao())
                .usuarioDestinoTitulo(cmd.usuarioDestinoTitulo())
                .unidadeDestinoSigla(cmd.unidadeDestinoSigla())
                .destinatario(cmd.destinatario())
                .assunto(cmd.assunto())
                .corpoHtml(cmd.corpoHtml())
                .situacao(SituacaoNotificacao.PENDENTE)
                .tentativas(0)
                .proximaTentativaEm(agora())
                .dataHoraCriacao(agora())
                .chaveIdempotencia(cmd.chaveIdempotencia())
                .build();

        try {
            return notificacaoEmailRepo.save(notificacao);
        } catch (DataIntegrityViolationException ex) {
            return notificacaoEmailRepo.findByChaveIdempotencia(cmd.chaveIdempotencia())
                    .orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacaoEmail> listarPendentes(int limite) {
        return notificacaoEmailRepo.findBySituacaoInAndProximaTentativaEmLessThanEqualOrderByDataHoraCriacaoAsc(
                List.of(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.FALHA_TEMPORARIA),
                agora(),
                PageRequest.of(0, limite)
        );
    }

    @Transactional(readOnly = true)
    public List<NotificacaoEmail> listarPorSubprocesso(Long subprocessoCodigo, int limite) {
        int tamanho = Math.clamp(limite, 1, LIMITE_CONSULTA_MAXIMO);
        return notificacaoEmailRepo.findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(
                subprocessoCodigo,
                PageRequest.of(0, tamanho)
        );
    }

    @Transactional(readOnly = true)
    public List<NotificacaoEmail> listarTodasAdmin(int limite) {
        int tamanho = Math.clamp(limite, 1, LIMITE_CONSULTA_MAXIMO);
        return notificacaoEmailRepo.findAllByOrderByDataHoraCriacaoDesc(PageRequest.of(0, tamanho));
    }

    @Transactional(readOnly = true)
    public List<NotificacaoSubprocessoResumoDto> listarResumoSubprocessosAtivos() {
        return notificacaoEmailRepo.resumirPorSubprocessosDeProcessosAtivos()
                .stream()
                .map(alertaDtoMapper::paraNotificacaoSubprocessoResumo)
                .toList();
    }

    public int reenfileirarFalhasDefinitivasPorSubprocesso(Long subprocessoCodigo) {
        return notificacaoEmailRepo.reenfileirarFalhasDefinitivasPorSubprocesso(subprocessoCodigo, agora());
    }

    public int reenviarPorCodigo(Long codigo) {
        return notificacaoEmailRepo.reenviarPorCodigo(codigo, agora());
    }

    public boolean marcarEnviandoSeDisponivel(NotificacaoEmail notificacao) {
        int atualizados = notificacaoEmailRepo.marcarEnviandoSeDisponivel(notificacao.getCodigo(), agora());
        if (atualizados == 0) {
            return false;
        }
        notificacao.setSituacao(SituacaoNotificacao.ENVIANDO);
        return true;
    }

    public void marcarEnviado(NotificacaoEmail notificacao) {
        notificacao.setSituacao(SituacaoNotificacao.ENVIADO);
        notificacao.setDataHoraEnvio(agora());
        notificacao.setUltimoErro(null);
        notificacaoEmailRepo.marcarEnviado(new NotificacaoEmailRepo.MarcarEnviadoCommand(
                notificacao.getCodigo(),
                notificacao.getDataHoraEnvio()
        ));
    }

    public void marcarFalha(NotificacaoEmail notificacao, Exception erro) {
        int tentativas = notificacao.getTentativas() + 1;
        notificacao.setTentativas(tentativas);
        notificacao.setUltimoErro(resumirErro(erro));
        if (tentativas >= MAX_TENTATIVAS) {
            notificacao.setSituacao(SituacaoNotificacao.FALHA_DEFINITIVA);
            notificacao.setProximaTentativaEm(null);
        } else {
            notificacao.setSituacao(SituacaoNotificacao.FALHA_TEMPORARIA);
            notificacao.setProximaTentativaEm(agora().plusSeconds(atrasoRetrySegundos(tentativas)));
        }
        notificacaoEmailRepo.marcarFalha(new NotificacaoEmailRepo.MarcarFalhaCommand(
                notificacao.getCodigo(),
                notificacao.getSituacao(),
                notificacao.getTentativas(),
                notificacao.getUltimoErro(),
                notificacao.getProximaTentativaEm()
        ));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    private long atrasoRetrySegundos(int tentativas) {
        return Math.min(300, (long) Math.pow(2, tentativas) * 10);
    }

    private String resumirErro(Exception erro) {
        String mensagem = erro.getMessage();
        String texto = mensagem == null || mensagem.isBlank() ? erro.getClass().getName() : mensagem;
        if (texto.length() <= LIMITE_ERRO) {
            return texto;
        }
        return texto.substring(0, LIMITE_ERRO);
    }
}
