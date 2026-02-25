package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.comum.ComumDtos.JustificativaRequest;
import sgc.comum.ComumDtos.TextoRequest;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;

/**
 * Controller para operações de validação de subprocessos.
 */
@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocesso Validação", description = "Endpoints para workflow de validação")
public class SubprocessoValidacaoController {

    private final SubprocessoFacade subprocessoFacade;
    private final AnaliseFacade analiseFacade;

    @PostMapping("/{codigo}/apresentar-sugestoes")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'APRESENTAR_SUGESTOES')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codigo,
            @RequestBody @Valid TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.apresentarSugestoes(codigo, request.texto(), usuario);
    }

    @GetMapping("/{codigo}/sugestoes")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public Map<String, Object> obterSugestoes(@PathVariable Long codigo) {
        return subprocessoFacade.obterSugestoes(codigo);
    }

    @GetMapping("/{codigo}/historico-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoValidacao(@PathVariable Long codigo) {
        return analiseFacade.listarHistoricoValidacao(codigo);
    }

    @PostMapping("/{codigo}/validar-mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VALIDAR_MAPA')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public ResponseEntity<Void> validarMapa(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.validarMapa(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/devolver-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DEVOLVER_MAPA')")
    @Operation(summary = "Devolve o mapa para ajuste (pelo chefe/gestor)")
    public ResponseEntity<Void> devolverValidacao(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.devolverValidacao(codigo, request.justificativa(), usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita a validação (pelo gestor)")
    public ResponseEntity<Void> aceitarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.aceitarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/homologar-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa a validação")
    public ResponseEntity<Void> homologarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.homologarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/submeter-mapa-ajustado")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Submete o mapa após ajustes solicitados")
    public ResponseEntity<Void> submeterMapaAjustado(
            @PathVariable Long codigo,
            @Valid @RequestBody SubmeterMapaAjustadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.submeterMapaAjustado(codigo, request, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.aceitarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codigo}/homologar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.homologarValidacaoEmBloco(request.subprocessos(), usuario);
    }
}
