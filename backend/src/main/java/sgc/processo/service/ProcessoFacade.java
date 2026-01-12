package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.*;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.eventos.EventoProcessoAtualizado;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.eventos.EventoProcessoExcluido;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static sgc.organizacao.model.TipoUnidade.INTERMEDIARIA;
import static sgc.processo.model.SituacaoProcesso.CRIADO;
import static sgc.processo.model.TipoProcesso.DIAGNOSTICO;
import static sgc.processo.model.TipoProcesso.REVISAO;

/**
 * Facade para orquestrar operações de Processo.
 *
 * <p>
 * Implementa o padrão Facade para simplificar a interface de uso e centralizar
 * a coordenação entre múltiplos serviços relacionados a processos.
 * 
 * <p><b>Nota sobre Injeção de Dependências:</b>
 * SubprocessoFacade é injetado normalmente. Dependência circular verificada e refutada.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoFacade {
    private final ProcessoRepo processoRepo;
    private final UnidadeService unidadeService;
    private final SubprocessoFacade subprocessoFacade;
    private final ApplicationEventPublisher publicadorEventos;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheBuilder processoDetalheBuilder;
    private final SubprocessoMapper subprocessoMapper;
    private final UsuarioService usuarioService;
    private final ProcessoInicializador processoInicializador;
    private final sgc.alerta.AlertaService alertaService;
    
    // Services especializados
    private final ProcessoAcessoService processoAcessoService;
    private final ProcessoValidador processoValidador;
    private final ProcessoFinalizador processoFinalizador;
    private final ProcessoConsultaService processoConsultaService;

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        return processoAcessoService.checarAcesso(authentication, codProcesso);
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
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(req.getUnidades()))
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
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(requisicao.getUnidades()))
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
        processoFinalizador.finalizar(codigo);
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

    // ========== LISTAGENS E CONSULTAS ==========

    public List<Long> listarUnidadesBloqueadasPorTipo(String tipo) {
        return processoConsultaService.listarUnidadesBloqueadasPorTipo(tipo);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public List<SubprocessoElegivelDto> listarSubprocessosElegiveis(Long codProcesso) {
        return processoConsultaService.listarSubprocessosElegiveis(codProcesso);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listarTodosSubprocessos(Long codProcesso) {
        return subprocessoFacade.listarEntidadesPorProcesso(codProcesso).stream()
                .map(subprocessoMapper::toDTO)
                .toList();
    }
}
