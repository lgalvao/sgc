package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.analise.dto.AnaliseMapper;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.modelo.TipoAnalise;
import sgc.sgrh.Usuario;
import sgc.subprocesso.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoValidacaoControle {
    private static final PolicyFactory SANITIZADOR_HTML = new HtmlPolicyBuilder().toFactory();

    private final SubprocessoWorkflowService subprocessoWorkflowService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final sgc.analise.AnaliseService analiseService;
    private final AnaliseMapper analiseMapper;

    @PostMapping("/{id}/disponibilizar-mapa")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disponibiliza o mapa de competências para as unidades")
    public ResponseEntity<RespostaDto> disponibilizarMapa(
            @PathVariable Long id,
            @RequestBody @Valid DisponibilizarMapaReq request,
            @AuthenticationPrincipal Usuario usuario) {

        var sanitizedObservacoes = SANITIZADOR_HTML.sanitize(request.observacoes());
        subprocessoWorkflowService.disponibilizarMapa(
                id,
                sanitizedObservacoes,
                request.dataLimiteEtapa2(),
                usuario
        );
        return ResponseEntity.ok(new RespostaDto("Mapa de competências disponibilizado com sucesso."));
    }

    @PostMapping("/{id}/apresentar-sugestoes")
    @Transactional
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long id,
            @RequestBody @Valid ApresentarSugestoesReq request,
            @AuthenticationPrincipal Usuario usuario) {

        var sanitizedSugestoes = SANITIZADOR_HTML.sanitize(request.sugestoes());
        subprocessoWorkflowService.apresentarSugestoes(
                id,
                sanitizedSugestoes,
                usuario.getTituloEleitoral()
        );
    }

    @PostMapping("/{id}/validar-mapa")
    @Transactional
    @Operation(summary = "Valida o mapa de competências da unidade")
    public void validarMapa(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.validarMapa(id, usuario.getTituloEleitoral());
    }

    @GetMapping("/{id}/sugestoes")
    public SugestoesDto obterSugestoes(@PathVariable Long id) {
        return subprocessoDtoService.obterSugestoes(id);
    }

    @GetMapping("/{id}/historico-validacao")
    public List<AnaliseValidacaoHistoricoDto> obterHistoricoValidacao(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO)
                .stream()
                .map(analiseMapper::toAnaliseValidacaoHistoricoDto)
                .toList();
    }

    @PostMapping("/{id}/devolver-validacao")
    @Transactional
    @Operation(summary = "Devolve a validação do mapa para a unidade de negócio")
    public void devolverValidacao(
            @PathVariable Long id,
            @RequestBody @Valid DevolverValidacaoReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        var sanitizedJustificativa = SANITIZADOR_HTML.sanitize(request.justificativa());

        subprocessoWorkflowService.devolverValidacao(
                id,
                sanitizedJustificativa,
                usuario
        );
    }

    @PostMapping("/{id}/aceitar-validacao")
    @Transactional
    @Operation(summary = "Aceita a validação do mapa")
    public void aceitarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.aceitarValidacao(id, usuario);
    }

    @PostMapping("/{id}/homologar-validacao")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa a validação do mapa")
    public void homologarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.homologarValidacao(id);
    }

    @PostMapping("/{id}/submeter-mapa-ajustado")
    @Transactional
    @Operation(summary = "Submete o mapa ajustado para nova validação")
    public void submeterMapaAjustado(
            @PathVariable Long id,
            @RequestBody @Valid SubmeterMapaAjustadoReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {

        var sanitizedObservacoes = SANITIZADOR_HTML.sanitize(request.observacoes());
        var sanitizedRequest = new SubmeterMapaAjustadoReq(sanitizedObservacoes, request.dataLimiteEtapa2());
        subprocessoWorkflowService.submeterMapaAjustado(id, sanitizedRequest, usuario.getTituloEleitoral());
    }
}
