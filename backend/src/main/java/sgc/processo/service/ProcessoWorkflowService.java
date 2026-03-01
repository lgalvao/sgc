package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static sgc.processo.model.SituacaoProcesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoWorkflowService {

    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final UnidadeRepo unidadeRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final UnidadeService unidadeService;
    private final SubprocessoService subprocessoService;
    private final ProcessoValidacaoService processoValidador;
    private final ProcessoNotificacaoService notificacaoService;

    @Transactional
    public void finalizar(Long codigo) {
        Processo processo = repo.buscar(Processo.class, codigo);
        processoValidador.validarFinalizacaoProcesso(processo);

        if (processo.getTipo() != TipoProcesso.DIAGNOSTICO) {
            tornarMapasVigentes(processo);
        }

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        notificacaoService.emailFinalizacaoProcesso(processo.getCodigo());

        log.info("Processo {} finalizado", codigo);
    }

    private void tornarMapasVigentes(Processo processo) {
        log.info("Mapa vigente definido para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Unidade unidade = subprocesso.getUnidade();
            Mapa mapa = subprocesso.getMapa();
            unidadeService.definirMapaVigente(unidade.getCodigo(), mapa);
        }
        log.info("Mapa(s) de {} subprocesso(s) definidos como vigentes.", subprocessos.size());
    }

    public List<String> iniciar(Long codigo, List<Long> codsUnidadesParam, Usuario usuario) {
        Processo processo = repo.buscar(Processo.class, codigo);

        validarSituacaoProcesso(processo);

        TipoProcesso tipo = processo.getTipo();
        List<Long> codigosUnidades;
        Set<Unidade> unidadesParaProcessar;

        if (tipo == TipoProcesso.REVISAO) {
            if (codsUnidadesParam == null || codsUnidadesParam.isEmpty()) {
                throw new ErroValidacao("A lista de unidades é obrigatória para iniciar o processo de revisão.");
            }
            codigosUnidades = codsUnidadesParam;
            unidadesParaProcessar = new HashSet<>(unidadeRepo.findAllById(codigosUnidades));

            Set<Unidade> snapshotArvore = carregarArvoreUnidades(unidadesParaProcessar);
            processo.sincronizarParticipantes(snapshotArvore);
        } else {
            List<Long> codsParticipantes = processo.getCodigosParticipantes();
            if (codsParticipantes.isEmpty()) {
                throw new ErroValidacao("Não há unidades participantes definidas para este processo.");
            }
            codigosUnidades = codsParticipantes;
            unidadesParaProcessar = new HashSet<>(unidadeRepo.findAllById(codigosUnidades));
        }

        List<String> erros = validarUnidades(tipo, codigosUnidades);
        if (!erros.isEmpty()) {
            return erros;
        }

        List<UnidadeMapa> unidadesMapas = List.of();
        if (tipo == TipoProcesso.REVISAO || tipo == TipoProcesso.DIAGNOSTICO) {
            unidadesMapas = unidadeMapaRepo.findAllById(codigosUnidades);
        }

        Unidade admin = repo.buscarPorSigla(Unidade.class, "ADMIN");
        criarSubprocessos(processo, tipo, codigosUnidades, unidadesParaProcessar, unidadesMapas, admin, usuario);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);
        notificacaoService.emailInicioProcesso(processo.getCodigo());

        int contagemUnidades = codigosUnidades.size();
        List<String> siglas = unidadeRepo.findSiglasByCodigos(codigosUnidades);
        log.info("Processo de {} {} iniciado para {} unidade(s): {}.",
                tipo.name().toLowerCase(), codigo, contagemUnidades, String.join(", ", siglas));
        return List.of();
    }

    private Set<Unidade> carregarArvoreUnidades(Set<Unidade> participantes) {
        Set<Unidade> arvore = new HashSet<>(participantes);
        for (Unidade u : participantes) {
            Unidade superior = u.getUnidadeSuperior();
            while (superior != null) {
                arvore.add(superior);
                superior = superior.getUnidadeSuperior();
            }
        }
        return arvore;
    }

    private void validarSituacaoProcesso(Processo processo) {
        if (processo.getSituacao() != CRIADO) {
            throw new ErroValidacao("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }
    }

    private List<String> validarUnidades(TipoProcesso tipo, List<Long> codigosUnidades) {
        List<String> erros = new ArrayList<>();

        if (tipo == TipoProcesso.REVISAO || tipo == TipoProcesso.DIAGNOSTICO) {
            processoValidador.getMensagemErroUnidadesSemMapa(codigosUnidades).ifPresent(erros::add);
        }

        getMensagemErroUnidadesEmProcessosAtivos(codigosUnidades).ifPresent(erros::add);

        return erros;
    }

    private void criarSubprocessos(Processo processo, TipoProcesso tipo,
                                   List<Long> codigosUnidades, Set<Unidade> unidadesParaProcessar,
                                   List<UnidadeMapa> unidadesMapas, Unidade admin, Usuario usuario) {

        Map<Long, UnidadeMapa> mapaUnidadeMapa = unidadesMapas.stream()
                .collect(Collectors.toMap(UnidadeMapa::getUnidadeCodigo, m -> m));

        switch (tipo) {
            case TipoProcesso.MAPEAMENTO ->
                    subprocessoService.criarParaMapeamento(processo, unidadesParaProcessar, admin, usuario);

            case TipoProcesso.REVISAO -> {
                for (Long codUnidade : codigosUnidades) {
                    Unidade unidade = repo.buscar(Unidade.class, codUnidade);
                    UnidadeMapa um = mapaUnidadeMapa.get(codUnidade);
                    subprocessoService.criarParaRevisao(processo, unidade, um, admin, usuario);
                }
            }

            case TipoProcesso.DIAGNOSTICO -> {
                for (Unidade unidade : unidadesParaProcessar) {
                    UnidadeMapa um = mapaUnidadeMapa.get(unidade.getCodigo());
                    subprocessoService.criarParaDiagnostico(processo, unidade, um, admin, usuario);
                }
            }
        }
    }

    private Optional<String> getMensagemErroUnidadesEmProcessosAtivos(List<Long> codsUnidades) {
        List<Long> unidadesBloqueadas = processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(
                SituacaoProcesso.EM_ANDAMENTO, codsUnidades);

        if (!unidadesBloqueadas.isEmpty()) {
            List<String> siglasUnidadesBloqueadas = unidadeRepo.findSiglasByCodigos(unidadesBloqueadas);
            return Optional.of("As seguintes unidades já participam de outro processo ativo: %s"
                    .formatted(String.join(", ", siglasUnidadesBloqueadas)));
        }
        return Optional.empty();
    }
}
