package sgc.diagnostico;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.comum.ComumDtos;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.service.*;
import sgc.seguranca.sanitizacao.UtilSanitizacao;

@RestController
@RequestMapping("/api/diagnosticos")
@RequiredArgsConstructor
@Tag(name = "Diagnóstico", description = "Endpoints do fluxo de diagnóstico de competências")
@PreAuthorize("isAuthenticated()")
public class DiagnosticoController {
    private final DiagnosticoConsultaService consultaService;
    private final DiagnosticoAvaliacaoService avaliacaoService;
    private final DiagnosticoFluxoService fluxoService;

    @GetMapping("/subprocessos/{codSubprocesso}/contexto")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_DIAGNOSTICO')")
    public ResponseEntity<DiagnosticoContextoDto> obterContexto(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterContexto(codSubprocesso));
    }

    @GetMapping("/subprocessos/{codSubprocesso}/autoavaliacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<AutoavaliacaoDto> obterAutoavaliacao(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterAutoavaliacao(codSubprocesso));
    }

    // CDU-40: autoavaliação com salvamento automático
    @PostMapping("/subprocessos/{codSubprocesso}/autoavaliacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<Void> salvarAutoavaliacao(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody AutoavaliacaoRequest request) {
        avaliacaoService.salvarAutoavaliacao(codSubprocesso, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subprocessos/{codSubprocesso}/autoavaliacao/concluir")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<Void> concluirAutoavaliacao(@PathVariable Long codSubprocesso) {
        avaliacaoService.concluirAutoavaliacao(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    // CDU-42: criar/editar consenso
    @PostMapping("/subprocessos/{codSubprocesso}/consenso/{servidorTitulo}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> salvarConsenso(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo,
            @Valid @RequestBody ConsensoRequest request) {
        avaliacaoService.salvarConsenso(codSubprocesso, request, servidorTitulo);
        return ResponseEntity.ok().build();
    }

    // CDU-43: consultar consenso do próprio servidor
    @GetMapping("/subprocessos/{codSubprocesso}/consenso")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<ConsensoDto> obterConsenso(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterConsenso(codSubprocesso));
    }

    @GetMapping("/subprocessos/{codSubprocesso}/consenso/{servidorTitulo}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<ConsensoDto> obterConsensoServidor(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo
    ) {
        return ResponseEntity.ok(consultaService.obterConsenso(codSubprocesso, servidorTitulo));
    }

    // CDU-44: aprovar consenso
    @PostMapping("/subprocessos/{codSubprocesso}/consenso/aprovar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'PREENCHER_AUTOAVALIACAO')")
    public ResponseEntity<Void> aprovarConsenso(@PathVariable Long codSubprocesso) {
        avaliacaoService.aprovarConsenso(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    // CDU-46: impossibilitar avaliação
    @PostMapping("/subprocessos/{codSubprocesso}/avaliacoes/{servidorTitulo}/impossibilitar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> impossibilitarAvaliacao(
            @PathVariable Long codSubprocesso,
            @PathVariable String servidorTitulo,
            @Valid @RequestBody ComumDtos.JustificativaRequest request) {
        avaliacaoService.impossibilitarAvaliacao(codSubprocesso, servidorTitulo, request.justificativa());
        return ResponseEntity.ok().build();
    }

    // CDU-45: ocupações críticas (salvamento automático)
    @PostMapping("/subprocessos/{codSubprocesso}/ocupacoes-criticas")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CRIAR_CONSENSO')")
    public ResponseEntity<Void> salvarOcupacoesCriticas(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody OcupacoesCriticasRequest request) {
        avaliacaoService.salvarOcupacoesCriticas(codSubprocesso, request);
        return ResponseEntity.ok().build();
    }

    // CDU-41: acompanhamento da equipe (lista de servidores e situações)
    @GetMapping("/subprocessos/{codSubprocesso}/equipe")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_DIAGNOSTICO')")
    public ResponseEntity<DiagnosticoEquipeDto> obterEquipe(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterEquipe(codSubprocesso));
    }

    // CDU-49: análise do diagnóstico da unidade (GESTOR/ADMIN)
    @GetMapping("/subprocessos/{codSubprocesso}/unidade")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_DIAGNOSTICO')")
    public ResponseEntity<DiagnosticoUnidadeDto> obterDiagnosticoUnidade(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterDiagnosticoUnidade(codSubprocesso));
    }

    @PostMapping("/subprocessos/{codSubprocesso}/concluir")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CONCLUIR_DIAGNOSTICO')")
    public ResponseEntity<Void> concluirDiagnostico(@PathVariable Long codSubprocesso) {
        fluxoService.concluirDiagnosticoUnidade(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subprocessos/{codSubprocesso}/validar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VALIDAR_DIAGNOSTICO')")
    public ResponseEntity<Void> validarDiagnostico(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) ComumDtos.TextoOpcionalRequest request) {
        String observacoes = sanitizarTextoOpcional(request);
        fluxoService.validarDiagnostico(codSubprocesso, observacoes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subprocessos/{codSubprocesso}/devolver")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_DIAGNOSTICO')")
    public ResponseEntity<Void> devolverDiagnostico(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody ComumDtos.JustificativaRequest request) {
        fluxoService.devolverDiagnostico(codSubprocesso, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subprocessos/{codSubprocesso}/homologar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_DIAGNOSTICO')")
    public ResponseEntity<Void> homologarDiagnostico(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) ComumDtos.TextoOpcionalRequest request) {
        String observacoes = sanitizarTextoOpcional(request);
        fluxoService.homologarDiagnostico(codSubprocesso, observacoes);
        return ResponseEntity.ok().build();
    }

    private @Nullable String sanitizarTextoOpcional(ComumDtos.@Nullable TextoOpcionalRequest request) {
        if (request == null || request.texto() == null) {
            return null;
        }
        return UtilSanitizacao.sanitizarFormatado(request.texto());
    }
}
