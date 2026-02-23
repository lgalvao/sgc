package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.comum.ComumDtos.JustificativaRequest;
import sgc.comum.ComumDtos.TextoRequest;
import sgc.organizacao.model.Usuario;
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
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'APRESENTAR_SUGESTOES')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codigo,
            @RequestBody @Valid TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.apresentarSugestoes(codigo, request.texto(), usuario);
    }

    @GetMapping("/{codigo}/sugestoes")
    @PreAuthorize("@subprocessoSecurity.canView(#codigo)")
    public Map<String, Object> obterSugestoes(@PathVariable Long codigo) {
        return subprocessoFacade.obterSugestoes(codigo);
    }

    @GetMapping("/{codigo}/historico-validacao")
    @PreAuthorize("@subprocessoSecurity.canView(#codigo)")
    public List<AnaliseHistoricoDto> obterHistoricoValidacao(@PathVariable Long codigo) {
        return analiseFacade.listarHistoricoValidacao(codigo);
    }

    @PostMapping("/{codigo}/validar-mapa")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'VALIDAR_MAPA')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public ResponseEntity<Void> validarMapa(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.validarMapa(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/devolver-validacao")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'DEVOLVER_MAPA')")
    @Operation(summary = "Devolve o mapa para ajuste (pelo chefe/gestor)")
    public ResponseEntity<Void> devolverValidacao(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.devolverValidacao(codigo, request.justificativa(), usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita a validação (pelo gestor)")
    public ResponseEntity<Void> aceitarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.aceitarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/homologar-validacao")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa a validação")
    public ResponseEntity<Void> homologarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.homologarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/submeter-mapa-ajustado")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'AJUSTAR_MAPA')")
    @Operation(summary = "Submete o mapa após ajustes solicitados")
    public ResponseEntity<Void> submeterMapaAjustado(
            @PathVariable Long codigo,
            @Valid @RequestBody SubmeterMapaAjustadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.submeterMapaAjustado(codigo, request, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao-bloco")
    @PreAuthorize("@subprocessoSecurity.canExecuteBulk(#request.subprocessos, 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.aceitarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codigo}/homologar-validacao-bloco")
    @PreAuthorize("@subprocessoSecurity.canExecuteBulk(#request.subprocessos, 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.homologarValidacaoEmBloco(request.subprocessos(), usuario);
    }
}
