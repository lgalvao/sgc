package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoContextoDto;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.eventos.EventoProcessoAtualizado;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.eventos.EventoProcessoExcluido;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static sgc.organizacao.model.TipoUnidade.INTERMEDIARIA;
import static sgc.processo.model.SituacaoProcesso.CRIADO;
import static sgc.processo.model.TipoProcesso.DIAGNOSTICO;
import static sgc.processo.model.TipoProcesso.REVISAO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO;

/**
 * Facade para orquestrar operações de Processo.
 *
 * <p>
 * Implementa o padrão Facade para simplificar a interface de uso e centralizar
 * a coordenação entre múltiplos serviços relacionados a processos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoFacade {
    private final ProcessoRepo processoRepo;
    private final UnidadeService unidadeService;
    private final SubprocessoService subprocessoService;
    private final ApplicationEventPublisher publicadorEventos;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheBuilder processoDetalheBuilder;
    private final SubprocessoMapper subprocessoMapper;
    private final UsuarioService usuarioService;
    private final ProcessoInicializador processoInicializador;
    private final sgc.alerta.AlertaService alertaService;

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return false;
        }

        String username = authentication.getName();
        boolean isGestorOuChefe = authentication.getAuthorities().stream()
                .anyMatch(
                        a -> "ROLE_GESTOR".equals(a.getAuthority())
                                || "ROLE_CHEFE".equals(a.getAuthority()));

        if (!isGestorOuChefe) {
            return false;
        }

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream()
                .findFirst()
                .map(PerfilDto::getUnidadeCodigo)
                .orElse(null);

        if (codUnidadeUsuario == null) {
            return false;
        }

        List<Long> codigosUnidadesHierarquia = buscarCodigosDescendentes(codUnidadeUsuario);

        return subprocessoService.verificarAcessoUnidadeAoProcesso(
                codProcesso, codigosUnidadesHierarquia);
    }

    private List<Long> buscarCodigosDescendentes(Long codUnidade) {
        List<Unidade> todasUnidades = unidadeService.buscarTodasEntidadesComHierarquia();

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
        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : req.getUnidades()) {
            Unidade unidade = unidadeService.buscarEntidadePorId(codigoUnidade);

            if (unidade.getTipo() == INTERMEDIARIA) {
                throw new ErroEstadoImpossivel("Erro interno: unidade não elegível foi enviada ao backend");
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

        // Captura estado anterior para o evento
        TipoProcesso tipoAnterior = processo.getTipo();
        Set<String> camposAlterados = new HashSet<>();

        if (!processo.getDescricao().equals(requisicao.getDescricao())) {
            camposAlterados.add("descricao");
        }
        if (processo.getTipo() != requisicao.getTipo()) {
            camposAlterados.add("tipo");
        }
        if (!Objects.equals(processo.getDataLimite(), requisicao.getDataLimiteEtapa1())) {
            camposAlterados.add("dataLimite");
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

        Set<Unidade> participantesAtuais = new HashSet<>(processo.getParticipantes());
        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : requisicao.getUnidades()) {
            participantes.add(unidadeService.buscarEntidadePorId(codigoUnidade));
        }

        if (!participantesAtuais.equals(participantes)) {
            camposAlterados.add("participantes");
        }

        processo.setParticipantes(participantes);

        Processo processoAtualizado = processoRepo.saveAndFlush(processo);
        log.info("Processo {} atualizado.", codigo);

        // Publica evento de atualização
        if (!camposAlterados.isEmpty()) {
            EventoProcessoAtualizado evento = EventoProcessoAtualizado.builder()
                    .processo(processoAtualizado)
                    .usuario(usuarioService.obterUsuarioAutenticado())
                    .camposAlterados(camposAlterados)
                    .dataHoraAtualizacao(LocalDateTime.now())
                    .tipoAnterior(tipoAnterior != requisicao.getTipo() ? tipoAnterior : null)
                    .build();
            publicadorEventos.publishEvent(evento);
            log.debug("Evento EventoProcessoAtualizado publicado para processo {}", codigo);
        }

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

        // Publica evento ANTES da exclusão para permitir listeners acessarem dados relacionados
        EventoProcessoExcluido evento = EventoProcessoExcluido.builder()
                .codProcesso(codigo)
                .descricao(processo.getDescricao())
                .tipo(processo.getTipo())
                .usuario(usuarioService.obterUsuarioAutenticado())
                .codigosUnidades(processo.getParticipantes().stream()
                        .map(Unidade::getCodigo)
                        .collect(Collectors.toSet()))
                .dataHoraExclusao(LocalDateTime.now())
                .build();
        publicadorEventos.publishEvent(evento);
        log.debug("Evento EventoProcessoExcluido publicado para processo {}", codigo);

        processoRepo.deleteById(codigo);
        log.info("Processo {} removido.", codigo);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long codigo) {
        return processoRepo.findById(codigo).map(processoMapper::toDto);
    }

    /**
     * Busca a entidade Processo por ID.
     * Método público para permitir uso por outros serviços (ex:
     * EventoProcessoListener).
     */
    @Transactional(readOnly = true)
    public Processo buscarEntidadePorId(Long codigo) {
        return processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoFacade.checarAcesso(authentication, #codigo)")
    public ProcessoContextoDto obterContextoCompleto(Long codigo) {
        ProcessoDetalheDto detalhes = obterDetalhes(codigo);
        List<SubprocessoElegivelDto> elegiveis = listarSubprocessosElegiveis(codigo);

        return ProcessoContextoDto.builder()
                .processo(detalhes)
                .elegiveis(elegiveis)
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoFacade.checarAcesso(authentication, #codProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long codProcesso) {
        Processo processo = processoRepo.findById(codProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));

        return processoDetalheBuilder.build(processo);
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarFinalizados() {
        return processoRepo.findBySituacaoOrderByDataFinalizacaoDesc(SituacaoProcesso.FINALIZADO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarAtivos() {
        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    public org.springframework.data.domain.Page<Processo> listarTodos(
            org.springframework.data.domain.Pageable pageable) {
        return processoRepo.findAll(pageable);
    }

    public org.springframework.data.domain.Page<Processo> listarPorParticipantesIgnorandoCriado(
            List<Long> unidadeIds, org.springframework.data.domain.Pageable pageable) {
        return processoRepo.findDistinctByParticipantes_CodigoInAndSituacaoNot(
                unidadeIds, SituacaoProcesso.CRIADO, pageable);
    }

    // ========== MÉTODOS DE INICIALIZAÇÃO (delegam para ProcessoInicializador)
    // ==========

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
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        publicadorEventos.publishEvent(
                new EventoProcessoFinalizado(processo.getCodigo(), LocalDateTime.now()));

        log.info("Processo {} finalizado", codigo);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void enviarLembrete(Long codProcesso, Long unidadeCodigo) {
        Processo processo = buscarEntidadePorId(codProcesso);
        Unidade unidade = unidadeService.buscarEntidadePorId(unidadeCodigo);

        // Verifica se unidade participa do processo
        if (processo.getParticipantes().stream().noneMatch(u -> u.getCodigo().equals(unidadeCodigo))) {
            throw new ErroProcesso("Unidade não participa deste processo.");
        }

        // Enviar alerta (CDU-34)
        String dataLimite = processo.getDataLimite().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String descricao = "Lembrete: Prazo do processo %s encerra em %s"
                .formatted(processo.getDescricao(), dataLimite);

        alertaService.criarAlertaSedoc(processo, unidade, descricao);
    }

    // ========== MÉTODOS PRIVADOS DE VALIDAÇÃO (usados apenas em criar/atualizar)
    // ==========

    private Optional<String> getMensagemErroUnidadesSemMapa(List<Long> codigosUnidades) {
        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            return Optional.empty();
        }
        List<Unidade> unidades = unidadeService.buscarEntidadesPorIds(codigosUnidades);

        List<Long> unidadesSemMapa = unidades.stream()
                .map(Unidade::getCodigo)
                .filter(codigo -> !unidadeService.verificarExistenciaMapaVigente(codigo))
                .toList();

        if (!unidadesSemMapa.isEmpty()) {
            List<String> siglasUnidadesSemMapa = unidadeService.buscarSiglasPorIds(unidadesSemMapa);
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
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(processo.getCodigo());
        List<String> pendentes = subprocessos.stream().filter(sp -> sp.getSituacao() != MAPEAMENTO_MAPA_HOMOLOGADO
                && sp.getSituacao() != REVISAO_MAPA_HOMOLOGADO)
                .map(sp -> {
                    String identificador = sp.getUnidade() != null ? sp.getUnidade().getSigla()
                            : String.format("Subprocesso %d", sp.getCodigo());
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
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Unidade unidade = Optional.ofNullable(subprocesso.getUnidade())
                    .orElseThrow(() -> new ErroProcesso(
                            "Subprocesso %d sem unidade associada.".formatted(subprocesso.getCodigo())));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                    .orElseThrow(() -> new ErroProcesso(
                            "Subprocesso %d sem mapa associado.".formatted(subprocesso.getCodigo())));

            unidadeService.definirMapaVigente(unidade.getCodigo(), mapaDoSubprocesso);
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
        if (authentication == null || authentication.getName() == null) {
            return List.of();
        }
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(codProcesso);
        if (isAdmin) {
            return subprocessos.stream()
                    .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO)
                    .map(this::toSubprocessoElegivelDto)
                    .toList();
        }

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream().findFirst().map(PerfilDto::getUnidadeCodigo).orElse(null);

        if (codUnidadeUsuario == null) {
            return List.of();
        }

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
        return subprocessoService.listarEntidadesPorProcesso(codProcesso).stream()
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
