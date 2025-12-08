package sgc.diagnostico;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.service.DiagnosticoService;

import java.util.List;

/**
 * Controller REST para o módulo de diagnóstico.
 */
@RestController
@RequestMapping("/api/diagnosticos")
@RequiredArgsConstructor
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    // TODO: Obter usuário autenticado via Spring Security
    // Por enquanto, vou mockar ou passar por header para facilitar testes se o security não estiver configurado para pegar contexto
    // Num sistema real: @AuthenticationPrincipal Usuario usuario
    // Vou assumir que o frontend passará o título do servidor no body quando necessário,
    // ou usaremos um header 'X-Usuario-Simulado' se estiver em dev.
    // Mas os requests já têm 'servidorTitulo'.
    // Para 'buscarMinhasAvaliacoes', vou pedir o servidorTitulo como param por enquanto.

    @GetMapping("/{subprocessoId}")
    public ResponseEntity<DiagnosticoDto> buscarDiagnostico(
            @PathVariable Long subprocessoId) {
        return ResponseEntity.ok(diagnosticoService.buscarDiagnosticoCompleto(subprocessoId));
    }

    @PostMapping("/{subprocessoId}/avaliacoes")
    public ResponseEntity<AvaliacaoServidorDto> salvarAvaliacao(
            @PathVariable Long subprocessoId,
            @RequestBody @Valid SalvarAvaliacaoRequest request,
            @RequestHeader(value = "X-Usuario-Simulado", required = false) String usuarioSimulado) {

        // Em produção usaria o usuário do contexto de segurança
        String servidorTitulo = usuarioSimulado != null ? usuarioSimulado : "123456789012";

        return ResponseEntity.ok(diagnosticoService.salvarAvaliacao(subprocessoId, servidorTitulo, request));
    }

    @GetMapping("/{subprocessoId}/avaliacoes/minhas")
    public ResponseEntity<List<AvaliacaoServidorDto>> buscarMinhasAvaliacoes(
            @PathVariable Long subprocessoId,
            @RequestParam(required = false) String servidorTitulo) { // Opcional se usar header/security

        String titulo = servidorTitulo != null ? servidorTitulo : "123456789012";
        return ResponseEntity.ok(diagnosticoService.buscarMinhasAvaliacoes(subprocessoId, titulo));
    }

    @PostMapping("/{subprocessoId}/avaliacoes/concluir")
    public ResponseEntity<Void> concluirAutoavaliacao(
            @PathVariable Long subprocessoId,
            @RequestBody(required = false) ConcluirAutoavaliacaoRequest request,
            @RequestHeader(value = "X-Usuario-Simulado", required = false) String usuarioSimulado) {

        String servidorTitulo = usuarioSimulado != null ? usuarioSimulado : "123456789012";
        if (request == null) request = new ConcluirAutoavaliacaoRequest(null);

        diagnosticoService.concluirAutoavaliacao(subprocessoId, servidorTitulo, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subprocessoId}/ocupacoes")
    public ResponseEntity<OcupacaoCriticaDto> salvarOcupacao(
            @PathVariable Long subprocessoId,
            @RequestBody @Valid SalvarOcupacaoRequest request) {
        return ResponseEntity.ok(diagnosticoService.salvarOcupacao(subprocessoId, request));
    }

    @GetMapping("/{subprocessoId}/ocupacoes")
    public ResponseEntity<List<OcupacaoCriticaDto>> buscarOcupacoes(
            @PathVariable Long subprocessoId) {
        return ResponseEntity.ok(diagnosticoService.buscarOcupacoes(subprocessoId));
    }

    @PostMapping("/{subprocessoId}/concluir")
    public ResponseEntity<DiagnosticoDto> concluirDiagnostico(
            @PathVariable Long subprocessoId,
            @RequestBody(required = false) ConcluirDiagnosticoRequest request) {

        if (request == null) request = new ConcluirDiagnosticoRequest(null);
        diagnosticoService.concluirDiagnostico(subprocessoId, request);

        return ResponseEntity.ok(diagnosticoService.buscarDiagnosticoCompleto(subprocessoId));
    }
}
