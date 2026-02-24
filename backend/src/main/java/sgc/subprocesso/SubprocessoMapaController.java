package sgc.subprocesso;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaViews;
import sgc.mapa.MapaFacade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocesso Mapa", description = "Endpoints para workflow do mapa de competências")
@Slf4j
public class SubprocessoMapaController {
    private final SubprocessoFacade subprocessoFacade;
    private final MapaFacade mapaFacade;

    @GetMapping("/{codigo}/impactos-mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VERIFICAR_IMPACTOS')")
    @JsonView(MapaViews.Publica.class)
    public ImpactoMapaResponse verificarImpactos(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocesso(codigo);
        return mapaFacade.verificarImpactos(subprocesso, usuario);
    }

    @GetMapping("/{codigo}/mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @JsonView(MapaViews.Publica.class)
    public Mapa obterMapa(@PathVariable Long codigo) {
        Subprocesso sp = subprocessoFacade.buscarSubprocessoComMapa(codigo);
        return sp.getMapa();
    }

    @PostMapping("/{codigo}/disponibilizar-mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza o mapa para validação")
    public ResponseEntity<MensagemResponse> disponibilizarMapa(
            @PathVariable Long codigo,
            @Valid @RequestBody DisponibilizarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.disponibilizarMapa(codigo, request, usuario);
        return ResponseEntity.ok(new MensagemResponse("Mapa de competências disponibilizado."));
    }

    @GetMapping("/{codigo}/mapa-visualizacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa formatado para visualização")
    @JsonView(MapaViews.Publica.class)
    public MapaVisualizacaoResponse obterMapaParaVisualizacao(@PathVariable Long codigo) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocesso(codigo);
        return mapaFacade.mapaParaVisualizacao(subprocesso);
    }

    @PostMapping("/{codigo}/mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva as alterações do mapa")
    @JsonView(MapaViews.Publica.class)
    public Mapa salvarMapa(
            @PathVariable Long codigo,
            @Valid @RequestBody SalvarMapaRequest request) {
        return subprocessoFacade.salvarMapaSubprocesso(codigo, request);
    }

    @GetMapping("/{codigo}/mapa-completo")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa completo para edição/visualização")
    @JsonView(MapaViews.Publica.class)
    @Transactional(readOnly = true)
    public ResponseEntity<Mapa> obterMapaCompleto(@PathVariable Long codigo) {
        try {
            Mapa mapa = mapaFacade.mapaCompletoPorSubprocesso(codigo);
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            log.error("Erro ao buscar mapa completo para subprocesso {}: {}", codigo, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{codigo}/mapa-completo")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva o mapa completo (batch)")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> salvarMapaCompleto(
            @PathVariable Long codigo,
            @Valid @RequestBody SalvarMapaRequest request) {

        Mapa mapa = subprocessoFacade.salvarMapaSubprocesso(codigo, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/disponibilizar-mapa-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza mapas em bloco")
    public void disponibilizarMapaEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        DisponibilizarMapaRequest dispoReq = DisponibilizarMapaRequest.builder()
                .dataLimite(request.dataLimite())
                .build();
        subprocessoFacade.disponibilizarMapaEmBloco(request.subprocessos(), codigo, dispoReq, usuario);
    }

    @GetMapping("/{codigo}/mapa-ajuste")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Obtém dados do mapa preparados para ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long codigo) {
        return subprocessoFacade.obterMapaParaAjuste(codigo);
    }

    @PostMapping("/{codigo}/mapa-ajuste/atualizar")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva os ajustes feitos no mapa")
    public void salvarAjustesMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarAjustesRequest request) {
        subprocessoFacade.salvarAjustesMapa(codigo, request.competencias());
    }

    @PostMapping("/{codigo}/competencia")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Adiciona uma competência ao mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> adicionarCompetencia(
            @PathVariable Long codigo,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoFacade.adicionarCompetencia(codigo, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencia/{codCompetencia}")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Atualiza uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> atualizarCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoFacade.atualizarCompetencia(codigo, codCompetencia, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencia/{codCompetencia}/remover")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Remove uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> removerCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia) {
        Mapa mapa = subprocessoFacade.removerCompetencia(codigo, codCompetencia);
        return ResponseEntity.ok(mapa);
    }
}
