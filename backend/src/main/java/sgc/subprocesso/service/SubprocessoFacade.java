package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoFacade {
    private final SubprocessoService subprocessoService;
    private final UsuarioFacade usuarioService;

    // ========================================================================
    // LEITURA
    // ========================================================================

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoService.buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return subprocessoService.buscarSubprocessoComMapa(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listar() {
        return subprocessoService.listarEntidades();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        return subprocessoService.listarEntidadesPorProcessoEUnidades(codProcesso, codUnidades);
    }

    @Transactional
    public Subprocesso criar(CriarSubprocessoRequest request) {
        return subprocessoService.criarEntidade(request);
    }

    @Transactional
    public Subprocesso atualizar(Long codigo, AtualizarSubprocessoRequest request) {
        return subprocessoService.atualizarEntidade(codigo, request);
    }

    @Transactional
    public void excluir(Long codigo) {
        subprocessoService.excluir(codigo);
    }

    @Transactional(readOnly = true)
    public SubprocessoDetalheResponse obterDetalhes(Long codigo) {
        Usuario usuario = usuarioService.usuarioAutenticado();
        return subprocessoService.obterDetalhes(codigo, usuario);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterSituacao(Long codigo) {
        return subprocessoService.obterStatus(codigo);
    }

    @Transactional(readOnly = true)
    public PermissoesSubprocessoDto obterPermissoesUI(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        return subprocessoService.obterPermissoesUI(sp, usuario);
    }

    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesSubprocesso(Long codigo) {
        return subprocessoService.listarAtividadesSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long codigo) {
        return subprocessoService.obterAtividadesSemConhecimento(codigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Mapa mapa) {
        return subprocessoService.obterAtividadesSemConhecimento(mapa);
    }

    @Transactional(readOnly = true)
    public ContextoEdicaoResponse obterContextoEdicao(Long codigo) {
        return subprocessoService.obterContextoEdicao(codigo);
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
    public Map<String, Object> obterSugestoes(Long codigo) {
        return Map.of("sugestoes", "");
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codigo) {
        return subprocessoService.obterMapaParaAjuste(codigo);
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

    // ========================================================================
    // WORKFLOW
    // ========================================================================

    @Transactional
    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        subprocessoService.atualizarParaEmAndamento(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarSubprocessosHomologados() {
        return subprocessoService.listarSubprocessosHomologados();
    }

    @Transactional
    public void disponibilizarCadastro(Long codigo, Usuario usuario) {
        subprocessoService.disponibilizarCadastro(codigo, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codigo, Usuario usuario) {
        subprocessoService.disponibilizarRevisao(codigo, usuario);
    }

    @Transactional
    public void devolverCadastro(Long codigo, String observacoes, Usuario usuario) {
        subprocessoService.devolverCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void aceitarCadastro(Long codigo, String observacoes, Usuario usuario) {
        subprocessoService.aceitarCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void homologarCadastro(Long codigo, String observacoes, Usuario usuario) {
        subprocessoService.homologarCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        subprocessoService.devolverRevisaoCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        subprocessoService.aceitarRevisaoCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codigo, String observacoes, Usuario usuario) {
        subprocessoService.homologarRevisaoCadastro(codigo, usuario, observacoes);
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            subprocessoService.aceitarCadastroEmBloco(ids, usuario);
        }
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            subprocessoService.homologarCadastroEmBloco(ids, usuario);
        }
    }

    // ========================================================================
    // MAPA WORKFLOW
    // ========================================================================

    @Transactional
    public Mapa salvarMapaSubprocesso(Long codigo, SalvarMapaRequest request) {
        return subprocessoService.salvarMapaSubprocesso(codigo, request);
    }

    @Transactional
    public void disponibilizarMapa(Long codigo, DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessoService.disponibilizarMapa(codigo, request, usuario);
    }

    @Transactional
    public void apresentarSugestoes(Long codigo, String sugestoes, Usuario usuario) {
        subprocessoService.apresentarSugestoes(codigo, sugestoes, usuario);
    }

    @Transactional
    public void validarMapa(Long codigo, Usuario usuario) {
        subprocessoService.validarMapa(codigo, usuario);
    }

    @Transactional
    public void devolverValidacao(Long codigo, String observacoes, Usuario usuario) {
        subprocessoService.devolverValidacao(codigo, observacoes, usuario);
    }

    @Transactional
    public void aceitarValidacao(Long codigo, Usuario usuario) {
        subprocessoService.aceitarValidacao(codigo, usuario);
    }

    @Transactional
    public void homologarValidacao(Long codigo, Usuario usuario) {
        subprocessoService.homologarValidacao(codigo, usuario);
    }

    @Transactional
    public void submeterMapaAjustado(Long codigo, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        subprocessoService.submeterMapaAjustado(codigo, request, usuario);
    }

    @Transactional
    public Mapa adicionarCompetencia(Long codigo, CompetenciaRequest request) {
        return subprocessoService.adicionarCompetencia(codigo, request);
    }

    @Transactional
    public Mapa atualizarCompetencia(Long codigo, Long codCompetencia, CompetenciaRequest request) {
        return subprocessoService.atualizarCompetencia(codigo, codCompetencia, request);
    }

    @Transactional
    public Mapa removerCompetencia(Long codigo, Long codCompetencia) {
        return subprocessoService.removerCompetencia(codigo, codCompetencia);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> ids, Long codProcesso, DisponibilizarMapaRequest request,
            Usuario usuario) {
        if (!ids.isEmpty()) {
            subprocessoService.disponibilizarMapaEmBloco(ids, request, usuario);
        }
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            subprocessoService.aceitarValidacaoEmBloco(ids, usuario);
        }
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> ids, Usuario usuario) {
        if (!ids.isEmpty()) {
            subprocessoService.homologarValidacaoEmBloco(ids, usuario);
        }
    }

    // ========================================================================
    // ADMIN / REABERTURA
    // ========================================================================

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        subprocessoService.reabrirCadastro(codigo, justificativa);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        subprocessoService.reabrirRevisaoCadastro(codigo, justificativa);
    }

    @Transactional
    public void alterarDataLimite(Long codigo, LocalDate novaDataLimite) {
        subprocessoService.alterarDataLimite(codigo, novaDataLimite);
    }

    @Transactional
    public void registrarMovimentacaoLembrete(Long codigo) {
        subprocessoService.registrarMovimentacaoLembrete(codigo);
    }

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        subprocessoService.salvarAjustesMapa(codSubprocesso, competencias);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        subprocessoService.importarAtividades(codSubprocessoDestino, codSubprocessoOrigem);
    }

    // ========================================================================
    // FACTORY
    // ========================================================================

    @Transactional
    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        subprocessoService.criarParaMapeamento(processo, unidades, unidadeOrigem, usuario);
    }

    @Transactional
    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        subprocessoService.criarParaRevisao(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
    }

    @Transactional
    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        subprocessoService.criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
    }
}
