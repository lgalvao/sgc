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
import sgc.comum.dto.ComumDtos.JustificativaRequest;
import sgc.comum.dto.ComumDtos.TextoRequest;
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
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codigo,
            @RequestBody @Valid TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.apresentarSugestoes(codigo, request.texto(), usuario);
    }

    @GetMapping("/{codigo}/sugestoes")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> obterSugestoes(@PathVariable Long codigo) {
        return subprocessoFacade.obterSugestoes(codigo);
    }

    @GetMapping("/{codigo}/historico-validacao")
    @PreAuthorize("isAuthenticated()")
    public List<AnaliseHistoricoDto> obterHistoricoValidacao(@PathVariable Long codigo) {
        return analiseFacade.listarHistoricoValidacao(codigo);
    }

    @PostMapping("/{codigo}/validar-mapa")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public ResponseEntity<Void> validarMapa(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.validarMapa(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/devolver-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Devolve o mapa para ajuste (pelo chefe/gestor)")
    public ResponseEntity<Void> devolverValidacao(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.devolverValidacao(codigo, request.justificativa(), usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Aceita a validação (pelo gestor)")
    public ResponseEntity<Void> aceitarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.aceitarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/homologar-validacao")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa a validação")
    public ResponseEntity<Void> homologarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.homologarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/submeter-mapa-ajustado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submete o mapa após ajustes solicitados")
    public ResponseEntity<Void> submeterMapaAjustado(
            @PathVariable Long codigo,
            @Valid @RequestBody SubmeterMapaAjustadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.submeterMapaAjustado(codigo, request, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao-bloco")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.aceitarValidacaoEmBloco(request.subprocessos(), codigo, usuario);
    }

    @PostMapping("/{codigo}/homologar-validacao-bloco")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.homologarValidacaoEmBloco(request.subprocessos(), codigo, usuario);
    }
}
