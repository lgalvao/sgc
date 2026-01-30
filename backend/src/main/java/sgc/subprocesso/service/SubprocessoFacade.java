package sgc.subprocesso.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.analise.AnaliseFacade;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.dto.SubprocessoCadastroDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.dto.SugestoesDto;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoAdminWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService;

/**
 * Facade para orquestrar operações de Subprocesso.
 *
 * <p>
 * Implementa o padrão Facade para simplificar a interface de uso e centralizar
 * a coordenação entre múltiplos serviços relacionados a subprocessos.
 *
 * <p>
 * Esta classe é o ponto de entrada único para todas as operações de
 * subprocesso,
 * delegando para serviços especializados internos que são package-private.
 *
 * @see SubprocessoCrudService Para operações CRUD básicas
 * @see SubprocessoValidacaoService Para validações
 * @see SubprocessoCadastroWorkflowService Para workflow de cadastro
 * @see SubprocessoMapaWorkflowService Para workflow de mapa
 * @see SubprocessoAdminWorkflowService Para operações administrativas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoFacade {

    // Services decomposed (package-private)
    private final SubprocessoCrudService crudService;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoCadastroWorkflowService cadastroWorkflowService;
    private final SubprocessoMapaWorkflowService mapaWorkflowService;
    private final SubprocessoAdminWorkflowService adminWorkflowService;
    private final SubprocessoAjusteMapaService ajusteMapaService;
    private final SubprocessoAtividadeService atividadeService;
    private final SubprocessoContextoService contextoService;
    private final SubprocessoPermissaoCalculator permissaoCalculator;

    // Utility services
    private final UsuarioFacade usuarioService;

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
        return contextoService.obterDetalhes(codigo, usuario);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterSituacao(Long codigo) {
        return crudService.obterStatus(codigo);
    }

    @Transactional(readOnly = true)
    public List<AtividadeVisualizacaoDto> listarAtividadesSubprocesso(Long codigo) {
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
    public ContextoEdicaoDto obterContextoEdicao(Long codigo, Perfil perfil) {
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
    public List<Subprocesso> listarPorProcessoESituacao(Long codProcesso, SituacaoSubprocesso situacao) {
        return crudService.listarPorProcessoESituacao(codProcesso, situacao);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade,
            List<SituacaoSubprocesso> situacoes) {
        return crudService.listarPorProcessoUnidadeESituacoes(codProcesso, codUnidade, situacoes);
    }

    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codigo) {
        return contextoService.obterSugestoes(codigo);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codigo) {
        return ajusteMapaService.obterMapaParaAjuste(codigo);
    }

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto obterPermissoes(Long codigo) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        return permissaoCalculator.obterPermissoes(codigo, usuario);
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
        adminWorkflowService.atualizarSituacaoParaEmAndamento(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarSubprocessosHomologados() {
        return adminWorkflowService.listarSubprocessosHomologados();
    }

    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long codigo) {
        return contextoService.obterCadastro(codigo);
    }

    // ===== Permissões =====

    @Transactional(readOnly = true)
    public void calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
        permissaoCalculator.calcularPermissoes(subprocesso, usuario);
    }

    // ===== Workflow de Cadastro =====

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
        cadastroWorkflowService.devolverCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.aceitarCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void homologarCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.homologarCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.devolverRevisaoCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.aceitarRevisaoCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        cadastroWorkflowService.homologarRevisaoCadastro(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        cadastroWorkflowService.aceitarCadastroEmBloco(codUnidades, usuario);
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        cadastroWorkflowService.homologarCadastroEmBloco(codUnidades, usuario);
    }

    // ===== Workflow de Mapa =====

    @Transactional
    public MapaCompletoDto salvarMapaSubprocesso(Long codigo, SalvarMapaRequest request) {
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
    public MapaCompletoDto adicionarCompetencia(Long codigo, CompetenciaRequest request) {
        return mapaWorkflowService.adicionarCompetencia(codigo, request);
    }

    @Transactional
    public MapaCompletoDto atualizarCompetencia(Long codigo, Long codCompetencia, CompetenciaRequest request) {
        return mapaWorkflowService.atualizarCompetencia(codigo, codCompetencia, request);
    }

    @Transactional
    public MapaCompletoDto removerCompetencia(Long codigo, Long codCompetencia) {
        return mapaWorkflowService.removerCompetencia(codigo, codCompetencia);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> codUnidades, Long codProcesso, DisponibilizarMapaRequest request,
            Usuario usuario) {
        mapaWorkflowService.disponibilizarMapaEmBloco(codUnidades, request, usuario);
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        mapaWorkflowService.aceitarValidacaoEmBloco(codUnidades, usuario);
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        mapaWorkflowService.homologarValidacaoEmBloco(codUnidades, usuario);
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
    public void alterarDataLimite(Long codigo, java.time.LocalDate novaDataLimite) {
        adminWorkflowService.alterarDataLimite(codigo, novaDataLimite);
    }

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        ajusteMapaService.salvarAjustesMapa(codSubprocesso, competencias);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        atividadeService.importarAtividades(codSubprocessoDestino, codSubprocessoOrigem);
    }
}