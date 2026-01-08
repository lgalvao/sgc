package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.decomposed.SubprocessoCrudService;
import sgc.subprocesso.service.decomposed.SubprocessoDetalheService;
import sgc.subprocesso.service.decomposed.SubprocessoValidacaoService;
import sgc.subprocesso.service.decomposed.SubprocessoWorkflowService;

import java.util.List;

/**
 * Facade para os serviços decompostos de Subprocesso.
 * Mantém a interface pública original para compatibilidade,
 * delegando para os novos serviços especializados.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class SubprocessoService {

    private final SubprocessoCrudService crudService;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoDetalheService detalheService;
    private final SubprocessoWorkflowService workflowService;
    private final UsuarioService usuarioService;

    // --- Métodos do SubprocessoCrudService ---

    public Subprocesso buscarSubprocesso(Long codigo) {
        return crudService.buscarSubprocesso(codigo);
    }

    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return crudService.buscarSubprocessoComMapa(codigo);
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
    public SubprocessoSituacaoDto obterSituacao(Long codSubprocesso) {
        return crudService.obterStatus(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return crudService.obterEntidadePorCodigoMapa(codMapa);
    }

    @Transactional
    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        return crudService.criar(subprocessoDto);
    }

    @Transactional
    public SubprocessoDto atualizar(Long codigo, SubprocessoDto subprocessoDto) {
        return crudService.atualizar(codigo, subprocessoDto);
    }

    @Transactional
    public void excluir(Long codigo) {
        crudService.excluir(codigo);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return crudService.listar();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return crudService.obterPorProcessoEUnidade(codProcesso, codUnidade);
    }

    // --- Métodos do SubprocessoDetalheService ---

    @Transactional(readOnly = true)
    public List<AtividadeVisualizacaoDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        return detalheService.listarAtividadesSubprocesso(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil) {
        Usuario usuario = obterUsuarioAutenticado();
        return detalheService.obterDetalhes(codigo, perfil, usuario);
    }

    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        return detalheService.obterCadastro(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codSubprocesso) {
        return detalheService.obterSugestoes(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        return detalheService.obterMapaParaAjuste(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto obterPermissoes(Long codSubprocesso) {
        Usuario usuario = obterUsuarioAutenticado();
        return detalheService.obterPermissoes(codSubprocesso, usuario);
    }

    // --- Métodos do SubprocessoValidacaoService ---

    @Transactional(readOnly = true)
    public void validarPermissaoEdicaoMapa(Long mapaCodigo, String tituloUsuario) {
        validacaoService.validarPermissaoEdicaoMapa(mapaCodigo, tituloUsuario);
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        return validacaoService.obterAtividadesSemConhecimento(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Mapa mapa) {
        return validacaoService.obterAtividadesSemConhecimento(mapa);
    }

    @Transactional(readOnly = true)
    public void validarExistenciaAtividades(Long codSubprocesso) {
        validacaoService.validarExistenciaAtividades(codSubprocesso);
    }

    public void validarAssociacoesMapa(Long mapaId) {
        validacaoService.validarAssociacoesMapa(mapaId);
    }

    @Transactional(readOnly = true)
    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        return validacaoService.validarCadastro(codSubprocesso);
    }

    // --- Métodos do SubprocessoWorkflowService ---

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, java.time.LocalDate novaDataLimite) {
        workflowService.alterarDataLimite(codSubprocesso, novaDataLimite);
    }

    @Transactional
    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        workflowService.atualizarSituacaoParaEmAndamento(mapaCodigo);
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return workflowService.listarSubprocessosHomologados();
    }

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        workflowService.reabrirCadastro(codigo, justificativa);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        workflowService.reabrirRevisaoCadastro(codigo, justificativa);
    }

    // --- Helper Methods ---

    private Usuario obterUsuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ErroAccessoNegado("Usuário não autenticado.");
        }
        String username = authentication.getName();
        return usuarioService.buscarPorLogin(username);
    }
}
