package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade para orquestrar operações de Subprocesso.
 *
 * <p>
 * Implementa o padrão Facade para simplificar a interface de uso e centralizar
 * a coordenação entre múltiplos serviços relacionados a subprocessos.
 * 
 * <p>
 * Esta classe é o ponto de entrada único para todas as operações de subprocesso,
 * delegando para serviços especializados internos que são package-private.
 * 
 * @see SubprocessoCrudService Para operações CRUD básicas
 * @see SubprocessoValidacaoService Para validações
 * @see SubprocessoWorkflowService Para operações de workflow (unificado)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoFacade {

    // Services decomposed (package-private)
    private final SubprocessoCrudService crudService;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoWorkflowService workflowService;
    
    // Utility services
    private final UsuarioFacade usuarioService;
    private final sgc.mapa.service.MapaFacade mapaFacade;
    
    // Dependencies for detail/context operations (previously in SubprocessoDetalheService/SubprocessoContextoService)
    private final AtividadeService atividadeService;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final SubprocessoDetalheMapper subprocessoDetalheMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final AnaliseFacade analiseFacade;
    private final CompetenciaService competenciaService;
    private final ConhecimentoService conhecimentoService;
    private final MapaAjusteMapper mapaAjusteMapper;
    private final sgc.seguranca.acesso.AccessControlService accessControlService;
    
    // Dependencies for map operations (from SubprocessoMapaService)
    private final sgc.subprocesso.model.SubprocessoRepo subprocessoRepo;
    private final sgc.subprocesso.model.SubprocessoMovimentacaoRepo movimentacaoRepo;
    private final sgc.mapa.service.CopiaMapaService copiaMapaService;
    private final sgc.mapa.mapper.AtividadeMapper atividadeMapper;

    // ===== Operações CRUD =====

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocesso(Long codigo) {
        return crudService.buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return crudService.buscarSubprocessoComMapa(codigo);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return crudService.listar();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return crudService.obterPorProcessoEUnidade(codProcesso, codUnidade);
    }

    @Transactional
    public SubprocessoDto criar(CriarSubprocessoRequest request) {
        return crudService.criar(request);
    }

    @Transactional
    public SubprocessoDto atualizar(Long codigo, AtualizarSubprocessoRequest request) {
        return crudService.atualizar(codigo, request);
    }

    @Transactional
    public void excluir(Long codigo) {
        crudService.excluir(codigo);
    }

    // ===== Consultas e Detalhes =====

    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        return obterDetalhesInterno(codigo, usuario);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterSituacao(Long codigo) {
        return crudService.obterStatus(codigo);
    }

    @Transactional(readOnly = true)
    public List<AtividadeVisualizacaoDto> listarAtividadesSubprocesso(Long codigo) {
        return listarAtividadesSubprocessoInterno(codigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long codigo) {
        return validacaoService.obterAtividadesSemConhecimento(codigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Mapa mapa) {
        return validacaoService.obterAtividadesSemConhecimento(mapa);
    }

    @Transactional(readOnly = true)
    public ContextoEdicaoDto obterContextoEdicao(Long codigo, Perfil perfil) {
        return obterContextoEdicaoInterno(codigo);
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return crudService.obterEntidadePorCodigoMapa(codMapa);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return crudService.verificarAcessoUnidadeAoProcesso(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return crudService.listarEntidadesPorProcesso(codProcesso);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoESituacao(Long codProcesso, SituacaoSubprocesso situacao) {
        return crudService.listarPorProcessoESituacao(codProcesso, situacao);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade, List<SituacaoSubprocesso> situacoes) {
        return crudService.listarPorProcessoUnidadeESituacoes(codProcesso, codUnidade, situacoes);
    }

    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codigo) {
        return obterSugestoesInterno(codigo);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codigo) {
        return obterMapaParaAjusteInterno(codigo);
    }

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto obterPermissoes(Long codigo) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        return obterPermissoesInterno(codigo, usuario);
    }

    @Transactional(readOnly = true)
    public ValidacaoCadastroDto validarCadastro(Long codigo) {
        return validacaoService.validarCadastro(codigo);
    }

    @Transactional
    public void validarExistenciaAtividades(Long codigo) {
        validacaoService.validarExistenciaAtividades(codigo);
    }

    @Transactional
    public void validarAssociacoesMapa(Long mapaId) {
        validacaoService.validarAssociacoesMapa(mapaId);
    }

    @Transactional
    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        workflowService.atualizarSituacaoParaEmAndamento(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarSubprocessosHomologados() {
        return workflowService.listarSubprocessosHomologados();
    }

    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long codigo) {
        return obterCadastroInterno(codigo);
    }

    // ===== Permissões =====

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
        return calcularPermissoesInterno(subprocesso, usuario);
    }

    // ===== Workflow de Cadastro =====

    @Transactional
    public void disponibilizarCadastro(Long codigo, Usuario usuario) {
        workflowService.disponibilizarCadastro(codigo, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codigo, Usuario usuario) {
        workflowService.disponibilizarRevisao(codigo, usuario);
    }

    @Transactional
    public void devolverCadastro(Long codigo, String observacoes, Usuario usuario) {
        workflowService.devolverCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarCadastro(Long codigo, String observacoes, Usuario usuario) {
        workflowService.aceitarCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void homologarCadastro(Long codigo, String observacoes, Usuario usuario) {
        workflowService.homologarCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        workflowService.devolverRevisaoCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        workflowService.aceitarRevisaoCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        workflowService.homologarRevisaoCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        workflowService.aceitarCadastroEmBloco(codUnidades, codProcesso, usuario);
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        workflowService.homologarCadastroEmBloco(codUnidades, codProcesso, usuario);
    }

    // ===== Workflow de Mapa =====

    @Transactional
    public MapaCompletoDto salvarMapaSubprocesso(Long codigo, SalvarMapaRequest request) {
        return workflowService.salvarMapaSubprocesso(codigo, request);
    }

    @Transactional
    public void disponibilizarMapa(Long codigo, DisponibilizarMapaRequest request, Usuario usuario) {
        workflowService.disponibilizarMapa(codigo, request, usuario);
    }

    @Transactional
    public void apresentarSugestoes(Long codigo, String sugestoes, Usuario usuario) {
        workflowService.apresentarSugestoes(codigo, sugestoes, usuario);
    }

    @Transactional
    public void validarMapa(Long codigo, Usuario usuario) {
        workflowService.validarMapa(codigo, usuario);
    }

    @Transactional
    public void devolverValidacao(Long codigo, String observacoes, Usuario usuario) {
        workflowService.devolverValidacao(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarValidacao(Long codigo, Usuario usuario) {
        workflowService.aceitarValidacao(codigo, usuario);
    }

    @Transactional
    public void homologarValidacao(Long codigo, Usuario usuario) {
        workflowService.homologarValidacao(codigo, usuario);
    }

    @Transactional
    public void submeterMapaAjustado(Long codigo, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        workflowService.submeterMapaAjustado(codigo, request, usuario);
    }

    @Transactional
    public MapaCompletoDto adicionarCompetencia(Long codigo, CompetenciaRequest request) {
        return workflowService.adicionarCompetencia(codigo, request);
    }

    @Transactional
    public MapaCompletoDto atualizarCompetencia(Long codigo, Long codCompetencia, CompetenciaRequest request) {
        return workflowService.atualizarCompetencia(codigo, codCompetencia, request);
    }

    @Transactional
    public MapaCompletoDto removerCompetencia(Long codigo, Long codCompetencia) {
        return workflowService.removerCompetencia(codigo, codCompetencia);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> codUnidades, Long codProcesso, DisponibilizarMapaRequest request, Usuario usuario) {
        workflowService.disponibilizarMapaEmBloco(codUnidades, codProcesso, request, usuario);
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        workflowService.aceitarValidacaoEmBloco(codUnidades, codProcesso, usuario);
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        workflowService.homologarValidacaoEmBloco(codUnidades, codProcesso, usuario);
    }

    // ===== Operações Administrativas =====

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        workflowService.reabrirCadastro(codigo, justificativa);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        workflowService.reabrirRevisaoCadastro(codigo, justificativa);
    }

    @Transactional
    public void alterarDataLimite(Long codigo, java.time.LocalDate novaDataLimite) {
        workflowService.alterarDataLimite(codigo, novaDataLimite);
    }

    // ===== Operações de Mapa =====

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias, String usuarioTituloEleitoral) {
        salvarAjustesMapaInterno(codSubprocesso, competencias, usuarioTituloEleitoral);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        importarAtividadesInterno(codSubprocessoDestino, codSubprocessoOrigem);
    }

    // ===== Private Helper Methods (from SubprocessoDetalheService and SubprocessoContextoService) =====

    private List<AtividadeVisualizacaoDto> listarAtividadesSubprocessoInterno(Long codSubprocesso) {
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        if (subprocesso.getMapa() == null) {
            return List.of();
        }
        // ⚡ Bolt: Usando 'buscarPorMapaCodigoComConhecimentos' para evitar N+1 queries
        // ao carregar conhecimentos para cada atividade
        List<Atividade> todasAtividades = atividadeService.buscarPorMapaCodigoComConhecimentos(subprocesso.getMapa().getCodigo());
        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    private AtividadeVisualizacaoDto mapAtividadeToDto(Atividade atividade) {
        List<ConhecimentoVisualizacaoDto> conhecimentosDto = atividade.getConhecimentos().stream()
                .map(c -> ConhecimentoVisualizacaoDto.builder()
                        .codigo(c.getCodigo())
                        .descricao(c.getDescricao())
                        .build())
                .toList();
        return AtividadeVisualizacaoDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(conhecimentosDto)
                .build();
    }

    private SubprocessoDetalheDto obterDetalhesInterno(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);
        
        // Centralized security check
        accessControlService.verificarPermissao(usuarioAutenticado, sgc.seguranca.acesso.Acao.VISUALIZAR_SUBPROCESSO, sp);

        Usuario responsavel = usuarioService.buscarResponsavelAtual(sp.getUnidade().getSigla());
        Usuario titular = null;
        if (sp.getUnidade().getTituloTitular() != null) {
            try {
                titular = usuarioService.buscarPorLogin(sp.getUnidade().getTituloTitular());
            } catch (Exception e) {
                log.warn("Erro ao buscar titular: {}", e.getMessage());
            }
        }

        List<Movimentacao> movimentacoes = repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        SubprocessoPermissoesDto permissoes = calcularPermissoesInterno(sp, usuarioAutenticado);

        return subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes);
    }

    private SubprocessoCadastroDto obterCadastroInterno(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        List<SubprocessoCadastroDto.AtividadeCadastroDto> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null) {
            List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
            for (Atividade a : atividades) {
                List<ConhecimentoDto> ksDto = a.getConhecimentos().stream().map(conhecimentoMapper::toDto).toList();
                atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDto.builder()
                        .codigo(a.getCodigo())
                        .descricao(a.getDescricao())
                        .conhecimentos(ksDto)
                        .build());
            }
        }
        return SubprocessoCadastroDto.builder()
                .subprocessoCodigo(sp.getCodigo())
                .unidadeSigla(sp.getUnidade().getSigla())
                .atividades(atividadesComConhecimentos)
                .build();
    }

    private SugestoesDto obterSugestoesInterno(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return SugestoesDto.of(sp);
    }

    private MapaAjusteDto obterMapaParaAjusteInterno(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocessoComMapa(codSubprocesso);
        Long codMapa = sp.getMapa().getCodigo();
        Analise analise = analiseFacade.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst().orElse(null);
        List<Competencia> competencias = competenciaService.buscarPorCodMapa(codMapa);
        List<Atividade> atividades = atividadeService.buscarPorMapaCodigo(codMapa);
        List<Conhecimento> conhecimentos = conhecimentoService.listarPorMapa(codMapa);
        @Nullable Analise analiseVal = analise;
        return mapaAjusteMapper.toDto(sp, analiseVal, competencias, atividades, conhecimentos);
    }

    private SubprocessoPermissoesDto obterPermissoesInterno(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return calcularPermissoesInterno(sp, usuario);
    }

    private SubprocessoPermissoesDto calcularPermissoesInterno(Subprocesso subprocesso, Usuario usuario) {
        // Determina as ações baseado no tipo de processo
        boolean isRevisao = subprocesso.getProcesso() != null 
                && subprocesso.getProcesso().getTipo() == sgc.processo.model.TipoProcesso.REVISAO;
        
        sgc.seguranca.acesso.Acao acaoDisponibilizarCadastro = isRevisao 
                ? sgc.seguranca.acesso.Acao.DISPONIBILIZAR_REVISAO_CADASTRO 
                : sgc.seguranca.acesso.Acao.DISPONIBILIZAR_CADASTRO;
        
        sgc.seguranca.acesso.Acao acaoDevolverCadastro = isRevisao 
                ? sgc.seguranca.acesso.Acao.DEVOLVER_REVISAO_CADASTRO 
                : sgc.seguranca.acesso.Acao.DEVOLVER_CADASTRO;
        
        sgc.seguranca.acesso.Acao acaoAceitarCadastro = isRevisao 
                ? sgc.seguranca.acesso.Acao.ACEITAR_REVISAO_CADASTRO 
                : sgc.seguranca.acesso.Acao.ACEITAR_CADASTRO;
        
        return SubprocessoPermissoesDto.builder()
                .podeVerPagina(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_SUBPROCESSO, subprocesso))
                .podeEditarMapa(podeExecutar(usuario, sgc.seguranca.acesso.Acao.EDITAR_MAPA, subprocesso))
                .podeVisualizarMapa(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_MAPA, subprocesso))
                .podeDisponibilizarMapa(podeExecutar(usuario, sgc.seguranca.acesso.Acao.DISPONIBILIZAR_MAPA, subprocesso))
                .podeDisponibilizarCadastro(podeExecutar(usuario, acaoDisponibilizarCadastro, subprocesso))
                .podeDevolverCadastro(podeExecutar(usuario, acaoDevolverCadastro, subprocesso))
                .podeAceitarCadastro(podeExecutar(usuario, acaoAceitarCadastro, subprocesso))
                .podeVisualizarDiagnostico(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_DIAGNOSTICO, subprocesso))
                .podeAlterarDataLimite(podeExecutar(usuario, sgc.seguranca.acesso.Acao.ALTERAR_DATA_LIMITE, subprocesso))
                .podeVisualizarImpacto(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VERIFICAR_IMPACTOS, subprocesso))
                .podeRealizarAutoavaliacao(podeExecutar(usuario, sgc.seguranca.acesso.Acao.REALIZAR_AUTOAVALIACAO, subprocesso))
                .podeReabrirCadastro(podeExecutar(usuario, sgc.seguranca.acesso.Acao.REABRIR_CADASTRO, subprocesso))
                .podeReabrirRevisao(podeExecutar(usuario, sgc.seguranca.acesso.Acao.REABRIR_REVISAO, subprocesso))
                .podeEnviarLembrete(podeExecutar(usuario, sgc.seguranca.acesso.Acao.ENVIAR_LEMBRETE_PROCESSO, subprocesso))
                .build();
    }

    private boolean podeExecutar(Usuario usuario, sgc.seguranca.acesso.Acao acao, Subprocesso subprocesso) {
        return accessControlService.podeExecutar(usuario, acao, subprocesso);
    }

    private ContextoEdicaoDto obterContextoEdicaoInterno(Long codSubprocesso) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        SubprocessoDetalheDto subprocessoDto = obterDetalhesInterno(codSubprocesso, usuario);
        String siglaUnidade = subprocessoDto.getUnidade().getSigla();
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        UnidadeDto unidadeDto = usuarioService.buscarUnidadePorSigla(siglaUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", siglaUnidade));

        MapaCompletoDto mapaDto = null;
        if (subprocesso.getMapa() != null) {
            mapaDto = mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
        }

        List<AtividadeVisualizacaoDto> atividades = listarAtividadesSubprocessoInterno(codSubprocesso);
        return ContextoEdicaoDto.builder()
                .unidade(unidadeDto)
                .subprocesso(subprocessoDto)
                .mapa(mapaDto)
                .atividadesDisponiveis(atividades)
                .build();
    }

    private void salvarAjustesMapaInterno(
            Long codSubprocesso,
            List<CompetenciaAjusteDto> competencias,
            String usuarioTituloEleitoral) {

        Subprocesso sp = subprocessoRepo
                .findById(codSubprocesso)
                .orElseThrow(() ->
                        new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida(
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", codSubprocesso);

        for (CompetenciaAjusteDto compDto : competencias) {
            var competencia = competenciaService.buscarPorCodigo(compDto.getCodCompetencia());

            competencia.setDescricao(compDto.getNome());

            java.util.Set<Atividade> atividades = new java.util.HashSet<>();
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                var atividade = atividadeService.obterPorCodigo(ativDto.getCodAtividade());

                sgc.mapa.dto.AtividadeDto dto = atividadeMapper.toDto(atividade);
                dto.setDescricao(ativDto.getNome());
                atividadeService.atualizar(atividade.getCodigo(), dto);

                atividades.add(atividadeService.obterPorCodigo(atividade.getCodigo()));
            }
            competencia.setAtividades(atividades);
            competenciaService.salvar(competencia);
        }

        sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    private void importarAtividadesInterno(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino = subprocessoRepo
                .findById(codSubprocessoDestino)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso de destino não encontrado: %d".formatted(codSubprocessoDestino)));

        if (spDestino.getSituacao() != SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != SituacaoSubprocesso.NAO_INICIADO) {

            throw new sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida("""
                    Atividades só podem ser importadas para um subprocesso
                    com cadastro em elaboração ou não iniciado.""");
        }

        Subprocesso spOrigem = subprocessoRepo.findById(codSubprocessoOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso de origem não encontrado: %d".formatted(codSubprocessoOrigem)));

        if (spOrigem.getMapa() == null || spDestino.getMapa() == null) {
            throw new sgc.subprocesso.erros.ErroMapaNaoAssociado("Subprocesso de origem ou destino não possui mapa associado.");
        }

        copiaMapaService.importarAtividadesDeOutroMapa(
                spOrigem.getMapa().getCodigo(),
                spDestino.getMapa().getCodigo()
        );

        if (spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();

            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                case null, default -> {
                    log.debug("Tipo de processo {} não requer atualização automática de situação no import.", tipoProcesso);
                }
            }
            subprocessoRepo.save(spDestino);
        }

        final sgc.organizacao.model.Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                unidadeOrigem != null ? unidadeOrigem.getSigla() : "N/A");

        movimentacaoRepo.save(new Movimentacao(
                spDestino,
                unidadeOrigem,
                spDestino.getUnidade(),
                descMovimentacao,
                null)
        );

        log.info("Atividades importadas do subprocesso {} para {}", codSubprocessoOrigem, codSubprocessoDestino);
    }
}
