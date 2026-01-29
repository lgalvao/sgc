package sgc.subprocesso.service.workflow;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.model.Subprocesso;

/**
 * Facade unificado responsável por todos os workflows de subprocesso.
 *
 * <p>
 * Consolidação dos serviços:
 * <ul>
 * <li>{@link SubprocessoCadastroWorkflowService} - Workflow de cadastro de
 * atividades</li>
 * <li>{@link SubprocessoMapaWorkflowService} - Workflow de mapa de competências</li>
 * <li>{@link SubprocessoAdminWorkflowService} - Operações administrativas</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SubprocessoWorkflowFacade {

    private final SubprocessoCadastroWorkflowService cadastroService;
    private final SubprocessoMapaWorkflowService mapaService;
    private final SubprocessoAdminWorkflowService adminService;

    // ===== OPERAÇÕES ADMINISTRATIVAS =====

    public void alterarDataLimite(Long codSubprocesso, java.time.LocalDate novaDataLimite) {
        adminService.alterarDataLimite(codSubprocesso, novaDataLimite);
    }

    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        adminService.atualizarSituacaoParaEmAndamento(mapaCodigo);
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return adminService.listarSubprocessosHomologados();
    }

    // ===== CADASTRO WORKFLOW =====

    public void reabrirCadastro(Long codigo, String justificativa) {
        cadastroService.reabrirCadastro(codigo, justificativa);
    }

    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        cadastroService.reabrirRevisaoCadastro(codigo, justificativa);
    }

    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        cadastroService.disponibilizarCadastro(codSubprocesso, usuario);
    }

    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        cadastroService.disponibilizarRevisao(codSubprocesso, usuario);
    }

    public void devolverCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        cadastroService.devolverCadastro(codSubprocesso, observacoes, usuario);
    }

    public void aceitarCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        cadastroService.aceitarCadastro(codSubprocesso, observacoes, usuario);
    }

    public void homologarCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        cadastroService.homologarCadastro(codSubprocesso, observacoes, usuario);
    }

    public void devolverRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        cadastroService.devolverRevisaoCadastro(codSubprocesso, observacoes, usuario);
    }

    public void aceitarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        cadastroService.aceitarRevisaoCadastro(codSubprocesso, observacoes, usuario);
    }

    public void homologarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        cadastroService.homologarRevisaoCadastro(codSubprocesso, observacoes, usuario);
    }

    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Long codSubprocessoBase, Usuario usuario) {
        // codSubprocessoBase mantido para compatibilidade, mas pode não ser usado se não necessário no service
        cadastroService.aceitarCadastroEmBloco(subprocessoCodigos, usuario);
    }

    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Long codSubprocessoBase, Usuario usuario) {
        cadastroService.homologarCadastroEmBloco(subprocessoCodigos, usuario);
    }

    // ===== MAPA WORKFLOW =====

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        return mapaService.salvarMapaSubprocesso(codSubprocesso, request);
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
        return mapaService.adicionarCompetencia(codSubprocesso, request);
    }

    public MapaCompletoDto atualizarCompetencia(Long codSubprocesso, Long codCompetencia, CompetenciaRequest request) {
        return mapaService.atualizarCompetencia(codSubprocesso, codCompetencia, request);
    }

    public MapaCompletoDto removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        return mapaService.removerCompetencia(codSubprocesso, codCompetencia);
    }

    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        mapaService.disponibilizarMapa(codSubprocesso, request, usuario);
    }

    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes, Usuario usuario) {
        mapaService.apresentarSugestoes(codSubprocesso, sugestoes, usuario);
    }

    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        mapaService.validarMapa(codSubprocesso, usuario);
    }

    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        mapaService.devolverValidacao(codSubprocesso, justificativa, usuario);
    }

    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        mapaService.aceitarValidacao(codSubprocesso, usuario);
    }

    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        mapaService.homologarValidacao(codSubprocesso, usuario);
    }

    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        mapaService.submeterMapaAjustado(codSubprocesso, request, usuario);
    }

    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos, Long codSubprocessoBase,
            DisponibilizarMapaRequest request, Usuario usuario) {
        mapaService.disponibilizarMapaEmBloco(subprocessoCodigos, request, usuario);
    }

    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos, Long codSubprocessoBase, Usuario usuario) {
        mapaService.aceitarValidacaoEmBloco(subprocessoCodigos, usuario);
    }

    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos, Long codSubprocessoBase, Usuario usuario) {
        mapaService.homologarValidacaoEmBloco(subprocessoCodigos, usuario);
    }
}