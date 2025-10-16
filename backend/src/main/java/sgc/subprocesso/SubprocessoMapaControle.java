package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.MapaService;
import sgc.mapa.MapaVisualizacaoService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.sgrh.Usuario;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesReq;
import sgc.subprocesso.modelo.Subprocesso;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoMapaControle {

    private final SubprocessoMapaService subprocessoMapaService;
    private final MapaService mapaService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final SubprocessoMapaWorkflowService subprocessoMapaWorkflowService;
    private final SubprocessoConsultaService subprocessoConsultaService;

    @GetMapping("/{id}/impactos-mapa")
    @Operation(summary = "Verifica os impactos da revisão no mapa de competências")
    public ImpactoMapaDto verificarImpactos(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return impactoMapaService.verificarImpactos(id, usuario);
    }

    @GetMapping("/{id}/mapa")
    public MapaCompletoDto obterMapa(@PathVariable Long id) {
        Subprocesso subprocesso = subprocessoConsultaService.getSubprocessoComMapa(id);
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), id);
    }

    @GetMapping("/{id}/mapa-visualizacao")
    public MapaVisualizacaoDto obterMapaVisualizacao(@PathVariable("id") Long subprocessoId) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(subprocessoId);
    }

    @PutMapping("/{id}/mapa")
    @Transactional
    public MapaCompletoDto salvarMapa(
            @PathVariable Long id,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return subprocessoMapaWorkflowService.salvarMapaSubprocesso(id, request, usuario.getTituloEleitoral());
    }

    @GetMapping("/{id}/mapa-ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long id) {
        return subprocessoDtoService.obterMapaParaAjuste(id);
    }

    @PutMapping("/{id}/mapa-ajuste")
    @Transactional
    public void salvarAjustesMapa(
            @PathVariable Long id,
            @RequestBody @Valid SalvarAjustesReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoMapaService.salvarAjustesMapa(
                id,
                request.competencias(),
                usuario.getTituloEleitoral()
        );
    }

    /**
     * CDU-15 - Obter mapa completo com competências e atividades aninhadas.
     * GET /api/subprocessos/{id}/mapa-completo
     */
    @GetMapping("/{id}/mapa-completo")
    @Operation(summary = "Obtém um mapa completo com competências e atividades (CDU-15)")
    public ResponseEntity<MapaCompletoDto> obterMapaCompleto(@PathVariable Long id) {
        Subprocesso subprocesso = subprocessoConsultaService.getSubprocessoComMapa(id);
        MapaCompletoDto mapa = mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), id);
        return ResponseEntity.ok(mapa);
    }

    /**
     * CDU-15 - Salvar mapa completo (criar/editar competências + vínculos).
     * PUT /api/subprocessos/{id}/mapa-completo
     */
    @PutMapping("/{id}/mapa-completo")
    @Transactional
    @Operation(summary = "Salva um mapa completo com competências e atividades (CDU-15)")
    public ResponseEntity<MapaCompletoDto> salvarMapaCompleto(
            @PathVariable Long id,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.salvarMapaSubprocesso(id, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }
}