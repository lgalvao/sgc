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
import sgc.sgrh.modelo.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoWorkflowService;

import java.util.List;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Transactional
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoValidacaoControle {
    private static final PolicyFactory SANITIZADOR_HTML = new HtmlPolicyBuilder().toFactory();

    private final SubprocessoWorkflowService subprocessoWorkflowService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final sgc.analise.AnaliseService analiseService;
    private final AnaliseMapper analiseMapper;

    /**
     * Disponibiliza o mapa de competências de um subprocesso para as unidades
     * envolvidas iniciarem a etapa de validação.
     * <p>
     * Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo observações e a data limite para a etapa.
     * @param usuario O usuário autenticado (administrador) que realiza a ação.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     */
    @PostMapping("/{codigo}/disponibilizar-mapa")

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disponibiliza o mapa de competências para as unidades")
    public ResponseEntity<RespostaDto> disponibilizarMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid DisponibilizarMapaReq request,
            @AuthenticationPrincipal Usuario usuario) {

        var sanitizedObservacoes = SANITIZADOR_HTML.sanitize(request.observacoes());
        subprocessoWorkflowService.disponibilizarMapa(
                codigo,
                sanitizedObservacoes,
                request.dataLimiteEtapa2(),
                usuario
        );
        return ResponseEntity.ok(new RespostaDto("Mapa de competências disponibilizado com sucesso."));
    }

    /**
     * Permite que um usuário apresente sugestões de melhoria para um mapa de competências.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo o texto das sugestões.
     * @param usuario O usuário autenticado que está enviando as sugestões.
     */
    @PostMapping("/{codigo}/apresentar-sugestoes")

    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codigo,
            @RequestBody @Valid ApresentarSugestoesReq request,
            @AuthenticationPrincipal Usuario usuario) {

        var sanitizedSugestoes = SANITIZADOR_HTML.sanitize(request.sugestoes());
        subprocessoWorkflowService.apresentarSugestoes(
                codigo,
                sanitizedSugestoes,
                usuario.getTituloEleitoral()
        );
    }

    /**
     * Registra a validação de um mapa de competências pelo responsável da unidade.
     *
     * @param codigo  O código do subprocesso.
     * @param usuario O usuário autenticado (chefe da unidade) que está validando.
     */
    @PostMapping("/{codigo}/validar-mapa")

    @Operation(summary = "Valida o mapa de competências da unidade")
    public void validarMapa(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.validarMapa(codigo, usuario.getTituloEleitoral());
    }

    /**
     * Obtém as sugestões de melhoria que foram apresentadas para o mapa de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link SugestoesDto} contendo as sugestões.
     */
    @GetMapping("/{codigo}/sugestoes")
    public SugestoesDto obterSugestoes(@PathVariable Long codigo) {
        return subprocessoDtoService.obterSugestoes(codigo);
    }

    /**
     * Obtém o histórico de análises da fase de validação de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Uma {@link List} de {@link AnaliseValidacaoHistoricoDto} com o histórico.
     */
    @GetMapping("/{codigo}/historico-validacao")
    public List<AnaliseValidacaoHistoricoDto> obterHistoricoValidacao(@PathVariable Long codigo) {
        return analiseService.listarPorSubprocesso(codigo, TipoAnalise.VALIDACAO)
                .stream()
                .map(analiseMapper::toAnaliseValidacaoHistoricoDto)
                .toList();
    }

    /**
     * Devolve a validação de um mapa para a unidade de negócio responsável para
     * que sejam feitos ajustes.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo a justificativa da devolução.
     * @param usuario O usuário autenticado que está realizando a devolução.
     */
    @PostMapping("/{codigo}/devolver-validacao")

    @Operation(summary = "Devolve a validação do mapa para a unidade de negócio")
    public void devolverValidacao(
            @PathVariable Long codigo,
            @RequestBody @Valid DevolverValidacaoReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        var sanitizedJustificativa = SANITIZADOR_HTML.sanitize(request.justificativa());

        subprocessoWorkflowService.devolverValidacao(
                codigo,
                sanitizedJustificativa,
                usuario
        );
    }

    /**
     * Aceita a validação de um mapa, movendo o subprocesso para a próxima etapa
     * de análise hierárquica.
     *
     * @param codigo  O código do subprocesso.
     * @param usuario O usuário autenticado que está aceitando a validação.
     */
    @PostMapping("/{codigo}/aceitar-validacao")

    @Operation(summary = "Aceita a validação do mapa")
    public void aceitarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.aceitarValidacao(codigo, usuario);
    }

    /**
     * Homologa a validação de um mapa, finalizando o fluxo de aprovações.
     * <p>
     * Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @param codigo  O código do subprocesso.
     * @param usuario O usuário autenticado (administrador) que realiza a homologação.
     */
    @PostMapping("/{codigo}/homologar-validacao")

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa a validação do mapa")
    public void homologarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.homologarValidacao(codigo);
    }

    /**
     * Submete a versão ajustada de um mapa para uma nova rodada de validação.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações e a nova data limite da etapa.
     * @param usuario O usuário autenticado que está submetendo os ajustes.
     */
    @PostMapping("/{codigo}/submeter-mapa-ajustado")

    @Operation(summary = "Submete o mapa ajustado para nova validação")
    public void submeterMapaAjustado(
            @PathVariable Long codigo,
            @RequestBody @Valid SubmeterMapaAjustadoReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {

        var sanitizedObservacoes = SANITIZADOR_HTML.sanitize(request.observacoes());
        var sanitizedRequest = new SubmeterMapaAjustadoReq(sanitizedObservacoes, request.dataLimiteEtapa2());
        subprocessoWorkflowService.submeterMapaAjustado(codigo, sanitizedRequest, usuario.getTituloEleitoral());
    }
}
