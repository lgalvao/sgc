package sgc.diagnostico;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.comum.ComumDtos;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.service.DiagnosticoAvaliacaoService;
import sgc.diagnostico.service.DiagnosticoConsultaService;
import sgc.diagnostico.service.DiagnosticoFluxoService;
import sgc.seguranca.sanitizacao.UtilSanitizacao;
import sgc.subprocesso.dto.AnaliseHistoricoDto;

import java.util.List;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Diagnóstico", description = "Endpoints do fluxo de diagnóstico de competências")
@PreAuthorize("isAuthenticated()")
public class DiagnosticoController {
    private final DiagnosticoConsultaService consultaService;
    private final DiagnosticoAvaliacaoService avaliacaoService;
    private final DiagnosticoFluxoService fluxoService;

    @GetMapping("/{codSubprocesso}/diagnostico/contexto")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_DIAGNOSTICO')")
    public ResponseEntity<DiagnosticoContextoDto> obterContexto(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterContexto(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/diagnostico/autoavaliacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<AutoavaliacaoDto> obterAutoavaliacao(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterAutoavaliacao(codSubprocesso));
    }

    @PostMapping("/{codSubprocesso}/diagnostico/autoavaliacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<Void> salvarAutoavaliacao(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody AutoavaliacaoRequest request) {
        avaliacaoService.salvarAutoavaliacao(codSubprocesso, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/autoavaliacao/concluir")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<Void> concluirAutoavaliacao(@PathVariable Long codSubprocesso) {
        avaliacaoService.concluirAutoavaliacao(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/consenso/{servidorTitulo}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> salvarConsenso(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo,
            @Valid @RequestBody ConsensoRequest request) {
        avaliacaoService.salvarConsenso(codSubprocesso, request, servidorTitulo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/consenso/{servidorTitulo}/concluir")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> concluirConsenso(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo) {
        avaliacaoService.concluirConsenso(codSubprocesso, servidorTitulo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/diagnostico/consenso")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<ConsensoDto> obterConsenso(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterConsenso(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/diagnostico/consenso/{servidorTitulo}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<ConsensoDto> obterConsensoServidor(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo
    ) {
        return ResponseEntity.ok(consultaService.obterConsenso(codSubprocesso, servidorTitulo));
    }

    @PostMapping("/{codSubprocesso}/diagnostico/consenso/aprovar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<Void> aprovarConsenso(@PathVariable Long codSubprocesso) {
        avaliacaoService.aprovarConsenso(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/avaliacoes/{servidorTitulo}/impossibilitar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> impossibilitarAvaliacao(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo,
            @Valid @RequestBody ComumDtos.JustificativaRequest request) {
        avaliacaoService.impossibilitarAvaliacao(codSubprocesso, servidorTitulo, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/avaliacoes/{servidorTitulo}/reverter-impossibilidade")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> reverterImpossibilidade(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo) {
        avaliacaoService.reverterImpossibilidade(codSubprocesso, servidorTitulo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/situacoes-capacitacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> salvarSituacoesCapacitacao(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody SituacoesCapacitacaoRequest request) {
        avaliacaoService.salvarSituacoesCapacitacao(codSubprocesso, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/diagnostico/equipe")
    @PreAuthorize("""
            hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')
            or hasPermission(#codSubprocesso, 'Subprocesso', 'VALIDAR_DIAGNOSTICO')
            or hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_DIAGNOSTICO')
            """)
    public ResponseEntity<DiagnosticoEquipeDto> obterEquipe(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterEquipe(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/diagnostico/unidade")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_DIAGNOSTICO')")
    public ResponseEntity<DiagnosticoUnidadeDto> obterDiagnosticoUnidade(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterDiagnosticoUnidade(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/diagnostico/historico")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_DIAGNOSTICO')")
    public ResponseEntity<List<AnaliseHistoricoDto>> listarHistoricoDiagnostico(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.listarHistoricoDiagnostico(codSubprocesso));
    }

    @PostMapping("/{codSubprocesso}/diagnostico/concluir")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CONCLUIR_DIAGNOSTICO')")
    public ResponseEntity<Void> concluirDiagnostico(@PathVariable Long codSubprocesso) {
        fluxoService.concluirDiagnosticoUnidade(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/diagnostico/concluir/validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CONCLUIR_DIAGNOSTICO')")
    public ResponseEntity<Void> validarConclusaoDiagnostico(@PathVariable Long codSubprocesso) {
        fluxoService.validarConclusaoDiagnosticoUnidade(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/validar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VALIDAR_DIAGNOSTICO')")
    public ResponseEntity<Void> validarDiagnostico(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) ComumDtos.TextoOpcionalRequest request) {
        String observacoes = sanitizarTextoOpcional(request);
        fluxoService.validarDiagnostico(codSubprocesso, observacoes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/diagnostico/validar/validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VALIDAR_DIAGNOSTICO')")
    public ResponseEntity<Void> validarAcaoValidarDiagnostico(@PathVariable Long codSubprocesso) {
        fluxoService.validarAceiteDiagnostico(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/devolver")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_DIAGNOSTICO')")
    public ResponseEntity<Void> devolverDiagnostico(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody ComumDtos.JustificativaRequest request) {
        fluxoService.devolverDiagnostico(codSubprocesso, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/diagnostico/devolver/validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_DIAGNOSTICO')")
    public ResponseEntity<Void> validarAcaoDevolverDiagnostico(@PathVariable Long codSubprocesso) {
        fluxoService.validarDevolucaoDiagnostico(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/diagnostico/homologar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_DIAGNOSTICO')")
    public ResponseEntity<Void> homologarDiagnostico(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) ComumDtos.TextoOpcionalRequest request) {
        String observacoes = sanitizarTextoOpcional(request);
        fluxoService.homologarDiagnostico(codSubprocesso, observacoes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/diagnostico/homologar/validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_DIAGNOSTICO')")
    public ResponseEntity<Void> validarAcaoHomologarDiagnostico(@PathVariable Long codSubprocesso) {
        fluxoService.validarHomologacaoDiagnostico(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    private @Nullable String sanitizarTextoOpcional(ComumDtos.@Nullable TextoOpcionalRequest request) {
        return request != null && request.texto() != null
                ? UtilSanitizacao.sanitizarFormatado(request.texto())
                : null;
    }
}
