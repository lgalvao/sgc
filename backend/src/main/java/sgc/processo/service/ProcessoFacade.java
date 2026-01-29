package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.*;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;

import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;

import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static sgc.processo.model.SituacaoProcesso.CRIADO;
import static sgc.processo.model.TipoProcesso.DIAGNOSTICO;
import static sgc.processo.model.TipoProcesso.REVISAO;

/**
 * Facade para orquestrar operações de Processo.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoFacade {
    private final ProcessoRepositoryService processoRepositoryService;
    private final UnidadeFacade unidadeService;
    private final SubprocessoFacade subprocessoFacade;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheBuilder processoDetalheBuilder;
    private final SubprocessoMapper subprocessoMapper;
    private final UsuarioFacade usuarioService;
    private final ProcessoInicializador processoInicializador;
    private final sgc.alerta.AlertaFacade alertaService;
    
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private ProcessoFacade self;

    // Services especializados
    private final ProcessoAcessoService processoAcessoService;
    private final ProcessoValidador processoValidador;
    private final ProcessoFinalizador processoFinalizador;
    private final ProcessoConsultaService processoConsultaService;

    private static final String ENTIDADE_PROCESSO = "Processo";

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        return processoAcessoService.checarAcesso(authentication, codProcesso);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProcessoDto criar(CriarProcessoRequest req) {
        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : req.unidades()) {
            Unidade unidade = unidadeService.buscarEntidadePorId(codigoUnidade);
            participantes.add(unidade);
        }

        TipoProcesso tipoProcesso = req.tipo();

        if (tipoProcesso == REVISAO || tipoProcesso == DIAGNOSTICO) {
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(req.unidades()))
                    .ifPresent(msg -> {
                        throw new ErroProcesso(msg);
                    });
        }

        Processo processo = new Processo()
                .setDescricao(req.descricao())
                .setTipo(tipoProcesso)
                .setDataLimite(req.dataLimiteEtapa1())
                .setSituacao(CRIADO)
                .setDataCriacao(LocalDateTime.now())
                .setParticipantes(participantes);

        Processo processoSalvo = processoRepositoryService.salvarEFlush(processo);

        log.info("Processo {} criado.", processoSalvo.getCodigo());

        return processoMapper.toDto(processoSalvo);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProcessoDto atualizar(Long codigo, AtualizarProcessoRequest requisicao) {
        Processo processo = processoRepositoryService.buscarPorId(codigo);

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        // Captura estado anterior para o evento
        TipoProcesso tipoAnterior = processo.getTipo();
        Set<String> camposAlterados = new HashSet<>();

        if (!processo.getDescricao().equals(requisicao.descricao())) {
            camposAlterados.add("descricao");
        }
        if (processo.getTipo() != requisicao.tipo()) {
            camposAlterados.add("tipo");
        }
        if (!Objects.equals(processo.getDataLimite(), requisicao.dataLimiteEtapa1())) {
            camposAlterados.add("dataLimite");
        }

        processo.setDescricao(requisicao.descricao());
        processo.setTipo(requisicao.tipo());
        processo.setDataLimite(requisicao.dataLimiteEtapa1());

        if (requisicao.tipo() == REVISAO || requisicao.tipo() == DIAGNOSTICO) {
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(requisicao.unidades()))
                    .ifPresent(msg -> {
                        throw new ErroProcesso(msg);
                    });
        }

        Set<Unidade> participantesAtuais = new HashSet<>(processo.getParticipantes());
        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : requisicao.unidades()) {
            participantes.add(unidadeService.buscarEntidadePorId(codigoUnidade));
        }

        if (!participantesAtuais.equals(participantes)) {
            camposAlterados.add("participantes");
        }

        processo.setParticipantes(participantes);

        Processo processoAtualizado = processoRepositoryService.salvarEFlush(processo);
        log.info("Processo {} atualizado.", codigo);

        return processoMapper.toDto(processoAtualizado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void apagar(Long codigo) {
        Processo processo = processoRepositoryService.buscarPorId(codigo);

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepositoryService.excluir(codigo);
        log.info("Processo {} removido.", codigo);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long codigo) {
        return processoRepositoryService.findById(codigo).map(processoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Processo buscarEntidadePorId(Long codigo) {
        return processoRepositoryService.buscarPorId(codigo);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoFacade.checarAcesso(authentication, #codigo)")
    public ProcessoDetalheDto obterContextoCompleto(Long codigo) {
        ProcessoDetalheDto detalhes = self.obterDetalhes(codigo);
        List<SubprocessoElegivelDto> elegiveis = self.listarSubprocessosElegiveis(codigo);

        detalhes.getElegiveis().addAll(elegiveis);
        return detalhes;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoFacade.checarAcesso(authentication, #codProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long codProcesso) {
        Processo processo = processoRepositoryService.buscarPorId(codProcesso);

        return processoDetalheBuilder.build(processo);
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarFinalizados() {
        return processoRepositoryService.findBySituacaoOrderByDataFinalizacaoDesc(SituacaoProcesso.FINALIZADO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarAtivos() {
        return processoRepositoryService.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    public org.springframework.data.domain.Page<Processo> listarTodos(
            org.springframework.data.domain.Pageable pageable) {
        return processoRepositoryService.findAll(pageable);
    }

    public org.springframework.data.domain.Page<Processo> listarPorParticipantesIgnorandoCriado(
            List<Long> unidadeIds, org.springframework.data.domain.Pageable pageable) {
        return processoRepositoryService.listarPorParticipantesIgnorandoSituacao(
                unidadeIds, CRIADO, pageable);
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
        Processo processo = self.buscarEntidadePorId(codProcesso);
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
    public Set<Long> buscarIdsUnidadesEmProcessosAtivos(Long codProcessoIgnorar) {
        return processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(codProcessoIgnorar);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public List<SubprocessoElegivelDto> listarSubprocessosElegiveis(Long codProcesso) {
        return processoConsultaService.listarSubprocessosElegiveis(codProcesso);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listarTodosSubprocessos(Long codProcesso) {
        return subprocessoFacade.listarEntidadesPorProcesso(codProcesso).stream()
                .map(subprocessoMapper::toDto)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    public void executarAcaoEmBloco(Long codProcesso, AcaoEmBlocoRequest req) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        if (req.acao() == sgc.processo.model.AcaoProcesso.DISPONIBILIZAR) {
             DisponibilizarMapaRequest dispReq = new DisponibilizarMapaRequest(
                req.dataLimite(), 
                "Disponibilização em bloco"
            );
            subprocessoFacade.disponibilizarMapaEmBloco(req.unidadeCodigos(), codProcesso, dispReq, usuario);
            return;
        }

        processarAcoesAceiteHomologacao(codProcesso, req, usuario);
    }

    private void processarAcoesAceiteHomologacao(Long codProcesso, AcaoEmBlocoRequest req, Usuario usuario) {
        List<Long> unidadesAceitarCadastro = new ArrayList<>();
        List<Long> unidadesAceitarValidacao = new ArrayList<>();
        List<Long> unidadesHomologarCadastro = new ArrayList<>();
        List<Long> unidadesHomologarValidacao = new ArrayList<>();

        for (Long codUnidade : req.unidadeCodigos()) {
             SubprocessoDto spDto = subprocessoFacade.obterPorProcessoEUnidade(codProcesso, codUnidade);
             
             if (req.acao() == sgc.processo.model.AcaoProcesso.ACEITAR) {
                 if (isSituacaoCadastro(spDto.getSituacao())) {
                     unidadesAceitarCadastro.add(codUnidade);
                 } else {
                     unidadesAceitarValidacao.add(codUnidade);
                 }
             } else if (req.acao() == sgc.processo.model.AcaoProcesso.HOMOLOGAR) {
                 if (isSituacaoCadastro(spDto.getSituacao())) {
                     unidadesHomologarCadastro.add(codUnidade);
                 } else {
                     unidadesHomologarValidacao.add(codUnidade);
                 }
             }
        }

        executarAcoesBatch(codProcesso, usuario, unidadesAceitarCadastro, unidadesAceitarValidacao, unidadesHomologarCadastro, unidadesHomologarValidacao);
    }

    private void executarAcoesBatch(Long codProcesso, 
        Usuario usuario, 
        List<Long> unidadesAceitarCadastro, 
        List<Long> unidadesAceitarValidacao, 
        List<Long> unidadesHomologarCadastro, 
        List<Long> unidadesHomologarValidacao) {

        if (!unidadesAceitarCadastro.isEmpty()) {
            subprocessoFacade.aceitarCadastroEmBloco(unidadesAceitarCadastro, codProcesso, usuario);
        }
        if (!unidadesAceitarValidacao.isEmpty()) {
            subprocessoFacade.aceitarValidacaoEmBloco(unidadesAceitarValidacao, codProcesso, usuario);
        }
        if (!unidadesHomologarCadastro.isEmpty()) {
            subprocessoFacade.homologarCadastroEmBloco(unidadesHomologarCadastro, codProcesso, usuario);
        }
        if (!unidadesHomologarValidacao.isEmpty()) {
            subprocessoFacade.homologarValidacaoEmBloco(unidadesHomologarValidacao, codProcesso, usuario);
        }
    }

    private boolean isSituacaoCadastro(sgc.subprocesso.model.SituacaoSubprocesso situacao) {
        return situacao == sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO || 
               situacao == sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
               situacao == sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA; 
    }
}
