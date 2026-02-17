package sgc.subprocesso;

import com.fasterxml.jackson.annotation.JsonView;
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
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaViews;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.model.Usuario;
import sgc.comum.dto.ComumDtos.JustificativaRequest;
import sgc.comum.dto.ComumDtos.TextoRequest;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;

/**
 * Controller para operações relacionadas ao Mapa de Competências dentro do workflow de Subprocesso.
 */
@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocesso Mapa", description = "Endpoints para workflow do mapa de competências")
public class SubprocessoMapaController {

    private final SubprocessoFacade subprocessoFacade;
    private final MapaFacade mapaFacade;
    private final AnaliseFacade analiseFacade;

    /**
     * Verifica os impactos que a disponibilização do mapa atual causará.
     */
    @GetMapping("/{codigo}/impactos-mapa")
    @PreAuthorize("isAuthenticated()")
    @JsonView(MapaViews.Publica.class)
    public ImpactoMapaResponse verificarImpactos(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocesso(codigo);
        return mapaFacade.verificarImpactos(subprocesso, usuario);
    }

    /**
     * Obtém o mapa associado ao subprocesso.
     */
    @GetMapping("/{codigo}/mapa")
    @PreAuthorize("isAuthenticated()")
    @JsonView(MapaViews.Publica.class)
    public Mapa obterMapa(@PathVariable Long codigo) {
        Subprocesso sp = subprocessoFacade.buscarSubprocessoComMapa(codigo);
        return sp.getMapa();
    }

    /**
     * Disponibiliza o mapa para validação.
     */
    @PostMapping("/{codigo}/disponibilizar-mapa")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disponibiliza o mapa para validação")
    public ResponseEntity<MensagemResponse> disponibilizarMapa(
            @PathVariable Long codigo,
            @Valid @RequestBody DisponibilizarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.disponibilizarMapa(codigo, request, usuario);
        return ResponseEntity.ok(new MensagemResponse("Mapa de competências disponibilizado."));
    }

    @GetMapping("/{codigo}/mapa-visualizacao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém o mapa formatado para visualização")
    @JsonView(MapaViews.Publica.class)
    public MapaVisualizacaoResponse obterMapaParaVisualizacao(@PathVariable Long codigo) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocesso(codigo);
        return mapaFacade.obterMapaParaVisualizacao(subprocesso);
    }

    /**
     * Salva as alterações feitas no mapa de competências.
     */
    @PostMapping("/{codigo}/mapa")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Salva as alterações do mapa")
    @JsonView(MapaViews.Publica.class)
    public Mapa salvarMapa(
            @PathVariable Long codigo,
            @Valid @RequestBody SalvarMapaRequest request) {
        return subprocessoFacade.salvarMapaSubprocesso(codigo, request);
    }

    /**
     * Obtém o mapa completo associado ao subprocesso para visualização ou edição.
     */
    @GetMapping("/{codigo}/mapa-completo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém o mapa completo para edição/visualização")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> obterMapaCompleto(@PathVariable Long codigo) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocessoComMapa(codigo);
        Mapa mapa = mapaFacade.obterPorCodigo(subprocesso.getMapa().getCodigo());
        return ResponseEntity.ok(mapa);
    }

    /**
     * Salva o mapa completo em uma única operação.
     */
    @PostMapping("/{codigo}/mapa-completo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Salva o mapa completo (batch)")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> salvarMapaCompleto(
            @PathVariable Long codigo,
            @Valid @RequestBody SalvarMapaRequest request) {

        Mapa mapa = subprocessoFacade.salvarMapaSubprocesso(codigo, request);
        return ResponseEntity.ok(mapa);
    }

    /**
     * Permite que um usuário apresente sugestões de melhoria para um mapa de competências.
     */
    @PostMapping("/{codigo}/apresentar-sugestoes")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codigo,
            @RequestBody @Valid TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.apresentarSugestoes(codigo, request.texto(), usuario);
    }

    /**
     * Obtém as sugestões de melhoria que foram apresentadas para o mapa de um subprocesso.
     */
    @GetMapping("/{codigo}/sugestoes")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> obterSugestoes(@PathVariable Long codigo) {
        return subprocessoFacade.obterSugestoes(codigo);
    }

    /**
     * Obtém o histórico de análises da fase de validação de um subprocesso.
     */
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

    @PostMapping("/{codigo}/disponibilizar-mapa-bloco")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém dados do mapa preparados para ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long codigo) {
        return subprocessoFacade.obterMapaParaAjuste(codigo);
    }

    @PostMapping("/{codigo}/mapa-ajuste/atualizar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Salva os ajustes feitos no mapa")
    public void salvarAjustesMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarAjustesRequest request) {
        subprocessoFacade.salvarAjustesMapa(codigo, request.competencias());
    }

    @PostMapping("/{codigo}/competencia")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adiciona uma competência ao mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> adicionarCompetencia(
            @PathVariable Long codigo,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoFacade.adicionarCompetencia(codigo, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencia/{codCompetencia}")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> removerCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia) {
        Mapa mapa = subprocessoFacade.removerCompetencia(codigo, codCompetencia);
        return ResponseEntity.ok(mapa);
    }
}
