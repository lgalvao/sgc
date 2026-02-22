package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.factory.SubprocessoFactory;
import sgc.subprocesso.service.workflow.SubprocessoAdminWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Orquestra operações de Subprocesso.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoFacade {
    private final SubprocessoCrudService crudService;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoCadastroWorkflowService cadastroWorkflowService;
    private final SubprocessoMapaWorkflowService mapaWorkflowService;
    private final SubprocessoAdminWorkflowService adminWorkflowService;
    private final SubprocessoAjusteMapaService ajusteMapaService;
    private final SubprocessoAtividadeService atividadeService;
    private final SubprocessoContextoService contextoService;
    private final SubprocessoFactory subprocessoFactory;
    private final UsuarioFacade usuarioService;

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocesso(Long codigo) {
        return crudService.buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return crudService.buscarSubprocessoComMapa(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listar() {
        return crudService.listarEntidades();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return crudService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return crudService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        return crudService.listarEntidadesPorProcessoEUnidades(codProcesso, codUnidades);
    }

    @Transactional
    public Subprocesso criar(CriarSubprocessoRequest request) {
        return crudService.criarEntidade(request);
    }

    @Transactional
    public Subprocesso atualizar(Long codigo, AtualizarSubprocessoRequest request) {
        return crudService.atualizarEntidade(codigo, request);
    }

    @Transactional
    public void excluir(Long codigo) {
        crudService.excluir(codigo);
    }

    // ===== Consultas e Detalhes =====

    @Transactional(readOnly = true)
    public SubprocessoDetalheResponse obterDetalhes(Long codigo) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        return contextoService.obterDetalhes(codigo, usuario);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterSituacao(Long codigo) {
        return crudService.obterStatus(codigo);
    }

    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesSubprocesso(Long codigo) {
        return atividadeService.listarAtividadesSubprocesso(codigo);
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
    public ContextoEdicaoResponse obterContextoEdicao(Long codigo) {
        return contextoService.obterContextoEdicao(codigo);
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
    public Map<String, Object> obterSugestoes(Long codigo) {
        return Map.of("sugestoes", "");
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codigo) {
        return ajusteMapaService.obterMapaParaAjuste(codigo);
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
        adminWorkflowService.atualizarParaEmAndamento(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarSubprocessosHomologados() {
        return adminWorkflowService.listarSubprocessosHomologados();
    }

    @Transactional
    public void disponibilizarCadastro(Long codigo, Usuario usuario) {
        cadastroWorkflowService.disponibilizarCadastro(codigo, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codigo, Usuario usuario) {
        cadastroWorkflowService.disponibilizarRevisao(codigo, usuario);
    }

    @Transactional
    public void devolverCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.devolverCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void aceitarCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.aceitarCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void homologarCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.homologarCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.devolverRevisaoCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.aceitarRevisaoCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.homologarRevisaoCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            cadastroWorkflowService.aceitarCadastroEmBloco(ids, usuario);
        }
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            cadastroWorkflowService.homologarCadastroEmBloco(ids, usuario);
        }
    }

    @Transactional
    public Mapa salvarMapaSubprocesso(Long codigo, SalvarMapaRequest request) {
        return mapaWorkflowService.salvarMapaSubprocesso(codigo, request);
    }

    @Transactional
    public void disponibilizarMapa(Long codigo, DisponibilizarMapaRequest request, Usuario usuario) {
        mapaWorkflowService.disponibilizarMapa(codigo, request, usuario);
    }

    @Transactional
    public void apresentarSugestoes(Long codigo, String sugestoes, Usuario usuario) {
        mapaWorkflowService.apresentarSugestoes(codigo, sugestoes, usuario);
    }

    @Transactional
    public void validarMapa(Long codigo, Usuario usuario) {
        mapaWorkflowService.validarMapa(codigo, usuario);
    }

    @Transactional
    public void devolverValidacao(Long codigo, String observacoes, Usuario usuario) {
        mapaWorkflowService.devolverValidacao(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarValidacao(Long codigo, Usuario usuario) {
        mapaWorkflowService.aceitarValidacao(codigo, usuario);
    }

    @Transactional
    public void homologarValidacao(Long codigo, Usuario usuario) {
        mapaWorkflowService.homologarValidacao(codigo, usuario);
    }

    @Transactional
    public void submeterMapaAjustado(Long codigo, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        mapaWorkflowService.submeterMapaAjustado(codigo, request, usuario);
    }

    @Transactional
    public Mapa adicionarCompetencia(Long codigo, CompetenciaRequest request) {
        return mapaWorkflowService.adicionarCompetencia(codigo, request);
    }

    @Transactional
    public Mapa atualizarCompetencia(Long codigo, Long codCompetencia, CompetenciaRequest request) {
        return mapaWorkflowService.atualizarCompetencia(codigo, codCompetencia, request);
    }

    @Transactional
    public Mapa removerCompetencia(Long codigo, Long codCompetencia) {
        return mapaWorkflowService.removerCompetencia(codigo, codCompetencia);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> codUnidades, Long codProcesso, DisponibilizarMapaRequest request,
            Usuario usuario) {

        List<Subprocesso> subprocessos = crudService.listarEntidadesPorProcessoEUnidades(codProcesso, codUnidades);
        List<Long> ids = subprocessos.stream().map(Subprocesso::getCodigo).toList();
        if (!ids.isEmpty()) {
            mapaWorkflowService.disponibilizarMapaEmBloco(ids, request, usuario);
        }
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            mapaWorkflowService.aceitarValidacaoEmBloco(ids, usuario);
        }
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            mapaWorkflowService.homologarValidacaoEmBloco(ids, usuario);
        }
    }

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        cadastroWorkflowService.reabrirCadastro(codigo, justificativa);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        cadastroWorkflowService.reabrirRevisaoCadastro(codigo, justificativa);
    }

    @Transactional
    public void alterarDataLimite(Long codigo, LocalDate novaDataLimite) {
        adminWorkflowService.alterarDataLimite(codigo, novaDataLimite);
    }

    @Transactional
    public void registrarMovimentacaoLembrete(Long codigo) {
        adminWorkflowService.registrarMovimentacaoLembrete(codigo);
    }

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        ajusteMapaService.salvarAjustesMapa(codSubprocesso, competencias);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        atividadeService.importarAtividades(codSubprocessoDestino, codSubprocessoOrigem);
    }

    @Transactional
    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        subprocessoFactory.criarParaMapeamento(processo, unidades, unidadeOrigem, usuario);
    }

    @Transactional
    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        subprocessoFactory.criarParaRevisao(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
    }

    @Transactional
    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        subprocessoFactory.criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
    }
}
