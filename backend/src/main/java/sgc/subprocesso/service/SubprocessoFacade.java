package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
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
 * @see SubprocessoService Para operações CRUD básicas
 * @see SubprocessoCadastroWorkflowService Para workflow de cadastro
 * @see SubprocessoMapaWorkflowService Para workflow de mapa
 * @see SubprocessoContextoService Para contexto de edição
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoFacade {

    private final SubprocessoService subprocessoService;
    private final SubprocessoCadastroWorkflowService cadastroWorkflowService;
    private final SubprocessoMapaWorkflowService mapaWorkflowService;
    private final SubprocessoContextoService contextoService;
    private final SubprocessoMapaService mapaService;
    private final SubprocessoPermissaoCalculator permissaoCalculator;

    // ===== Operações CRUD =====

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoService.buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return subprocessoService.buscarSubprocessoComMapa(codigo);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return subprocessoService.listar();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoService.obterPorProcessoEUnidade(codProcesso, codUnidade);
    }

    @Transactional
    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        return subprocessoService.criar(subprocessoDto);
    }

    @Transactional
    public SubprocessoDto atualizar(Long codigo, SubprocessoDto subprocessoDto) {
        return subprocessoService.atualizar(codigo, subprocessoDto);
    }

    @Transactional
    public void excluir(Long codigo) {
        subprocessoService.excluir(codigo);
    }

    // ===== Consultas e Detalhes =====

    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil) {
        return subprocessoService.obterDetalhes(codigo, perfil);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterSituacao(Long codigo) {
        return subprocessoService.obterSituacao(codigo);
    }

    @Transactional(readOnly = true)
    public List<AtividadeVisualizacaoDto> listarAtividadesSubprocesso(Long codigo) {
        return subprocessoService.listarAtividadesSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long codigo) {
        return subprocessoService.obterAtividadesSemConhecimento(codigo);
    }

    @Transactional(readOnly = true)
    public ContextoEdicaoDto obterContextoEdicao(Long codigo, Perfil perfil) {
        return contextoService.obterContextoEdicao(codigo, perfil);
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return subprocessoService.obterEntidadePorCodigoMapa(codMapa);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return subprocessoService.verificarAcessoUnidadeAoProcesso(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoService.listarEntidadesPorProcesso(codProcesso);
    }

    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codigo) {
        return subprocessoService.obterSugestoes(codigo);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codigo) {
        return subprocessoService.obterMapaParaAjuste(codigo);
    }

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto obterPermissoes(Long codigo) {
        return subprocessoService.obterPermissoes(codigo);
    }

    @Transactional(readOnly = true)
    public ValidacaoCadastroDto validarCadastro(Long codigo) {
        return subprocessoService.validarCadastro(codigo);
    }

    @Transactional
    public void validarExistenciaAtividades(Long codigo) {
        subprocessoService.validarExistenciaAtividades(codigo);
    }

    @Transactional
    public void validarAssociacoesMapa(Long mapaId) {
        subprocessoService.validarAssociacoesMapa(mapaId);
    }

    @Transactional
    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        subprocessoService.atualizarSituacaoParaEmAndamento(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarSubprocessosHomologados() {
        return subprocessoService.listarSubprocessosHomologados();
    }

    // ===== Permissões =====

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
        return permissaoCalculator.calcular(subprocesso, usuario);
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
        cadastroWorkflowService.aceitarCadastroEmBloco(codUnidades, codProcesso, usuario);
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        cadastroWorkflowService.homologarCadastroEmBloco(codUnidades, codProcesso, usuario);
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
    public void submeterMapaAjustado(Long codigo, SubmeterMapaAjustadoReq request, Usuario usuario) {
        mapaWorkflowService.submeterMapaAjustado(codigo, request, usuario);
    }

    @Transactional
    public MapaCompletoDto adicionarCompetencia(Long codigo, CompetenciaReq request) {
        return mapaWorkflowService.adicionarCompetencia(codigo, request);
    }

    @Transactional
    public MapaCompletoDto atualizarCompetencia(Long codigo, Long codCompetencia, CompetenciaReq request) {
        return mapaWorkflowService.atualizarCompetencia(codigo, codCompetencia, request);
    }

    @Transactional
    public MapaCompletoDto removerCompetencia(Long codigo, Long codCompetencia) {
        return mapaWorkflowService.removerCompetencia(codigo, codCompetencia);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> codUnidades, Long codProcesso, DisponibilizarMapaRequest request, Usuario usuario) {
        mapaWorkflowService.disponibilizarMapaEmBloco(codUnidades, codProcesso, request, usuario);
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        mapaWorkflowService.aceitarValidacaoEmBloco(codUnidades, codProcesso, usuario);
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> codUnidades, Long codProcesso, Usuario usuario) {
        mapaWorkflowService.homologarValidacaoEmBloco(codUnidades, codProcesso, usuario);
    }

    // ===== Operações Administrativas =====

    @Transactional
    public void reabrirCadastro(Long codigo, Usuario usuario) {
        cadastroWorkflowService.reabrirCadastro(codigo, usuario);
    }

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        subprocessoService.reabrirCadastro(codigo, justificativa);
    }

    @Transactional
    public void reabrirRevisao(Long codigo, Usuario usuario) {
        cadastroWorkflowService.reabrirRevisao(codigo, usuario);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        subprocessoService.reabrirRevisaoCadastro(codigo, justificativa);
    }

    @Transactional
    public void alterarDataLimite(Long codigo, java.time.LocalDate novaDataLimite) {
        subprocessoService.alterarDataLimite(codigo, novaDataLimite);
    }

    // ===== Operações de Mapa (delegadas ao SubprocessoMapaService) =====

    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaComDetalhes(Long codMapa, Long codSubprocesso) {
        return mapaService.obterMapaComDetalhes(codMapa, codSubprocesso);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        mapaService.importarAtividades(codSubprocessoDestino, codSubprocessoOrigem);
    }

    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long codigo) {
        return subprocessoService.obterCadastro(codigo);
    }
}
