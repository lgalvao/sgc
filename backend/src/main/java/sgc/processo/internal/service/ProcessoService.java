package sgc.processo.internal.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.internal.model.Mapa;
import sgc.processo.api.*;
import sgc.processo.internal.mappers.ProcessoMapper;
import sgc.processo.internal.erros.ErroProcesso;
import sgc.processo.internal.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.api.eventos.EventoProcessoCriado;
import sgc.processo.api.eventos.EventoProcessoFinalizado;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.api.PerfilDto;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.api.SubprocessoDto;
import sgc.subprocesso.internal.mappers.SubprocessoMapper;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.*;

import static sgc.processo.internal.model.SituacaoProcesso.CRIADO;
import static sgc.processo.internal.model.TipoProcesso.DIAGNOSTICO;
import static sgc.processo.internal.model.TipoProcesso.REVISAO;
import static sgc.subprocesso.internal.model.SituacaoSubprocesso.*;
import static sgc.unidade.internal.model.TipoUnidade.INTERMEDIARIA;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final sgc.unidade.internal.model.UnidadeMapaRepo unidadeMapaRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final ApplicationEventPublisher publicadorEventos;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheBuilder processoDetalheBuilder;
    private final SubprocessoMapper subprocessoMapper;
    private final SgrhService sgrhService;
    private final ProcessoInicializador processoInicializador;

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        String username = authentication.getName();
        boolean isGestorOuChefe =
                authentication.getAuthorities().stream()
                        .anyMatch(
                                a ->
                                        "ROLE_GESTOR".equals(a.getAuthority())
                                                || "ROLE_CHEFE".equals(a.getAuthority()));

        if (!isGestorOuChefe) {
            return false;
        }

        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario =
                perfis.stream()
                        .findFirst()
                        .map(sgc.sgrh.api.PerfilDto::getUnidadeCodigo)
                        .orElse(null);

        if (codUnidadeUsuario == null) {
            log.debug("checarAcesso: codUnidadeUsuario é null para usuário {}", username);
            return false;
        }

        List<Long> codigosUnidadesHierarquia = buscarCodigosDescendentes(codUnidadeUsuario);
        log.debug("checarAcesso: usuário {} (unidade {}) tem acesso a {} unidades na hierarquia: {}",
                username, codUnidadeUsuario, codigosUnidadesHierarquia.size(), codigosUnidadesHierarquia);

        if (codigosUnidadesHierarquia.isEmpty()) {
            log.warn("checarAcesso: busca hierárquica retornou lista vazia para unidade {}", codUnidadeUsuario);
            return false;
        }

        boolean temAcesso = subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(
                codProcesso, codigosUnidadesHierarquia);
        log.debug("checarAcesso: usuário {} {} acesso ao processo {}",
                username, temAcesso ? "TEM" : "NÃO TEM", codProcesso);
        return temAcesso;
    }

    private List<Long> buscarCodigosDescendentes(Long codUnidade) {
        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();

        Map<Long, List<Unidade>> mapaPorPai = new HashMap<>();
        for (Unidade u : todasUnidades) {
            if (u.getUnidadeSuperior() != null) {
                mapaPorPai.computeIfAbsent(u.getUnidadeSuperior().getCodigo(), k -> new ArrayList<>()).add(u);
            }
        }

        List<Long> resultado = new ArrayList<>();
        Queue<Long> fila = new LinkedList<>();
        Set<Long> visitados = new HashSet<>();

        fila.add(codUnidade);
        visitados.add(codUnidade);

        while (!fila.isEmpty()) {
            Long atual = fila.poll();
            resultado.add(atual);

            List<Unidade> filhos = mapaPorPai.get(atual);
            if (filhos != null) {
                for (Unidade filho : filhos) {
                    if (!visitados.contains(filho.getCodigo())) {
                        visitados.add(filho.getCodigo());
                        fila.add(filho.getCodigo());
                    }
                }
            }
        }

        return resultado;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProcessoDto criar(CriarProcessoReq req) {
        if (req.getDescricao() == null || req.getDescricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }

        if (req.getUnidades().isEmpty()) {
            throw new ConstraintViolationException(
                    "Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : req.getUnidades()) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigoUnidade));

            if (unidade.getTipo() == INTERMEDIARIA) {
                log.error("ERRO INTERNO: Tentativa de criar processo com unidade INTERMEDIARIA: {}", unidade.getSigla());
                throw new IllegalStateException("Erro interno: unidade não elegível foi enviada ao backend");
            }
            participantes.add(unidade);
        }

        TipoProcesso tipoProcesso = req.getTipo();

        if (tipoProcesso == REVISAO || tipoProcesso == DIAGNOSTICO) {
            getMensagemErroUnidadesSemMapa(new ArrayList<>(req.getUnidades()))
                    .ifPresent(msg -> {
                        throw new ErroProcesso(msg);
                    });
        }

        Processo processo = new Processo()
                .setDescricao(req.getDescricao())
                .setTipo(tipoProcesso)
                .setDataLimite(req.getDataLimiteEtapa1())
                .setSituacao(CRIADO)
                .setDataCriacao(LocalDateTime.now())
                .setParticipantes(participantes);

        Processo processoSalvo = processoRepo.saveAndFlush(processo);

        publicadorEventos.publishEvent(new EventoProcessoCriado(this, processoSalvo.getCodigo()));
        log.info("Processo {} criado.", processoSalvo.getCodigo());

        return processoMapper.toDto(processoSalvo);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProcessoDto atualizar(Long codigo, AtualizarProcessoReq requisicao) {
        Processo processo = processoRepo.findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());

        if (requisicao.getTipo() == REVISAO || requisicao.getTipo() == DIAGNOSTICO) {
            getMensagemErroUnidadesSemMapa(new ArrayList<>(requisicao.getUnidades()))
                    .ifPresent(msg -> {
                        throw new ErroProcesso(msg);
                    });
        }

        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : requisicao.getUnidades()) {
            participantes.add(unidadeRepo.findById(codigoUnidade)
                            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigoUnidade)));
        }
        processo.setParticipantes(participantes);

        Processo processoAtualizado = processoRepo.saveAndFlush(processo);
        log.info("Processo {} atualizado.", codigo);

        return processoMapper.toDto(processoAtualizado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void apagar(Long codigo) {
        Processo processo = processoRepo.findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepo.deleteById(codigo);
        log.info("Processo {} removido.", codigo);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long codigo) {
        return processoRepo.findById(codigo).map(processoMapper::toDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    public ProcessoContextoDto obterContextoCompleto(Long codigo) {
        ProcessoDetalheDto detalhes = obterDetalhes(codigo);
        List<SubprocessoElegivelDto> elegiveis = listarSubprocessosElegiveis(codigo);

        return ProcessoContextoDto.builder()
                .processo(detalhes)
                .elegiveis(elegiveis)
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long codProcesso) {
        // Bolt: Use optimized query to fetch participants and their hierarchy in one go
        Processo processo = processoRepo.findByIdWithParticipantes(codProcesso)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));

        return processoDetalheBuilder.build(processo);
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarFinalizados() {
        return processoRepo.findBySituacao(SituacaoProcesso.FINALIZADO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarAtivos() {
        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    // ========== MÉTODOS DE INICIALIZAÇÃO (delegam para ProcessoInicializador) ==========

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<String> iniciarProcessoMapeamento(Long codigo, List<Long> codsUnidades) {
        return processoInicializador.iniciar(codigo, codsUnidades);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<String> iniciarProcessoRevisao(Long codigo, List<Long> codigosUnidades) {
        return processoInicializador.iniciar(codigo, codigosUnidades);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<String> iniciarProcessoDiagnostico(Long codigo, List<Long> codsUnidades) {
        return processoInicializador.iniciar(codigo, codsUnidades);
    }

    // ========== FINALIZAÇÃO ==========

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void finalizar(Long codigo) {
        log.debug("Iniciando finalização do processo: código={}", codigo);

        Processo processo = processoRepo.findById(codigo).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        publicadorEventos.publishEvent(
                new EventoProcessoFinalizado(processo.getCodigo(), LocalDateTime.now()));

        log.info("Processo {} finalizado", codigo);
    }

    // ========== MÉTODOS PRIVADOS DE VALIDAÇÃO (usados apenas em criar/atualizar) ==========

    private Optional<String> getMensagemErroUnidadesSemMapa(List<Long> codigosUnidades) {
        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            return Optional.empty();
        }
        List<Unidade> unidades = unidadeRepo.findAllById(codigosUnidades);

        List<Long> unidadesSemMapa = unidades.stream()
                        .map(Unidade::getCodigo)
                        .filter(codigo -> !unidadeMapaRepo.existsById(codigo))
                        .toList();

        if (!unidadesSemMapa.isEmpty()) {
            List<String> siglasUnidadesSemMapa = unidadeRepo.findSiglasByCodigos(unidadesSemMapa);
            return Optional.of(("As seguintes unidades não possuem mapa vigente e não podem participar"
                    + " de um processo de revisão: %s").formatted(String.join(", ", siglasUnidadesSemMapa)));
        }
        return Optional.empty();
    }

    private void validarFinalizacaoProcesso(Processo processo) {
        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new ErroProcesso("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }
        validarTodosSubprocessosHomologados(processo);
    }

    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        List<String> pendentes = subprocessos.stream().filter(sp -> sp.getSituacao() != MAPEAMENTO_MAPA_HOMOLOGADO
                                                && sp.getSituacao() != REVISAO_MAPA_HOMOLOGADO)
                        .map(sp -> {String identificador = sp.getUnidade() != null ? sp.getUnidade().getSigla() : String.format("Subprocesso %d", sp.getCodigo());
                                    return String.format("%s (Situação: %s)", identificador, sp.getSituacao());
                                })
                        .toList();

        if (!pendentes.isEmpty()) {
            String mensagem = String.format("Não é possível encerrar o processo. Unidades pendentes de"
                                    + " homologação:%n- %s",
                            String.join("%n- ", pendentes));
            log.warn("Validação de finalização falhou: {} subprocessos não homologados.", pendentes.size());
            throw new ErroProcesso(mensagem);
        }
        log.info("Homologados {} subprocessos.", subprocessos.size());
    }

    private void tornarMapasVigentes(Processo processo) {
        log.info("Mapa vigente definido para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos =
                subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Unidade unidade = Optional.ofNullable(subprocesso.getUnidade())
                    .orElseThrow(() -> new ErroProcesso("Subprocesso %d sem unidade associada.".formatted(subprocesso.getCodigo())));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                    .orElseThrow(() -> new ErroProcesso("Subprocesso %d sem mapa associado.".formatted(subprocesso.getCodigo())));

            sgc.unidade.internal.model.UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(unidade.getCodigo())
                    .orElse(new sgc.unidade.internal.model.UnidadeMapa());
            unidadeMapa.setUnidadeCodigo(unidade.getCodigo());
            unidadeMapa.setMapaVigente(mapaDoSubprocesso);
            unidadeMapaRepo.save(unidadeMapa);

            log.debug("Mapa vigente para unidade {} definido como mapa {}",
                    unidade.getCodigo(),
                    mapaDoSubprocesso.getCodigo());
        }
        log.info("Mapa(s) de {} subprocesso(s) definidos como vigentes.", subprocessos.size());
    }

    // ========== LISTAGENS E CONSULTAS ==========

    public List<Long> listarUnidadesBloqueadasPorTipo(String tipo) {
        TipoProcesso tipoProcesso = TipoProcesso.valueOf(tipo);

        return processoRepo.findUnidadeCodigosBySituacaoAndTipo(SituacaoProcesso.EM_ANDAMENTO, tipoProcesso);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public List<SubprocessoElegivelDto> listarSubprocessosElegiveis(Long codProcesso) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        List<Subprocesso> subprocessos =
                subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);
        if (isAdmin) {
            return subprocessos.stream()
                    .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO)
                    .map(this::toSubprocessoElegivelDto)
                    .toList();
        }

        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream().findFirst().map(PerfilDto::getUnidadeCodigo).orElse(null);

        if (codUnidadeUsuario == null) return List.of();

        return subprocessos.stream()
                .filter(sp -> sp.getUnidade() != null
                        && sp.getUnidade().getCodigo().equals(codUnidadeUsuario))
                .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                        || sp.getSituacao() == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                .map(this::toSubprocessoElegivelDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listarTodosSubprocessos(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso).stream()
                .map(subprocessoMapper::toDTO)
                .toList();
    }

    private SubprocessoElegivelDto toSubprocessoElegivelDto(Subprocesso sp) {
        return SubprocessoElegivelDto.builder()
                .codSubprocesso(sp.getCodigo())
                .unidadeNome(sp.getUnidade().getNome())
                .unidadeSigla(sp.getUnidade().getSigla())
                .situacao(sp.getSituacao())
                .build();
    }
}
