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

    /**
     * Disponibiliza o mapa de competências de um subprocesso para as unidades
     * envolvidas iniciarem a etapa de validação.
     * <p>
     * Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo observações e a data limite para a etapa.
     * @param usuario O usuário autenticado (administrador) que realiza a ação.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     */
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

    /**
     * Permite que um usuário apresente sugestões de melhoria para um mapa de competências.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo o texto das sugestões.
     * @param usuario O usuário autenticado que está enviando as sugestões.
     */
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

    /**
     * Registra a validação de um mapa de competências pelo responsável da unidade.
     *
     * @param id      O ID do subprocesso.
     * @param usuario O usuário autenticado (chefe da unidade) que está validando.
     */
    @PostMapping("/{id}/validar-mapa")
    @Transactional
    @Operation(summary = "Valida o mapa de competências da unidade")
    public void validarMapa(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.validarMapa(id, usuario.getTituloEleitoral());
    }

    /**
     * Obtém as sugestões de melhoria que foram apresentadas para o mapa de um subprocesso.
     *
     * @param id O ID do subprocesso.
     * @return Um {@link SugestoesDto} contendo as sugestões.
     */
    @GetMapping("/{id}/sugestoes")
    public SugestoesDto obterSugestoes(@PathVariable Long id) {
        return subprocessoDtoService.obterSugestoes(id);
    }

    /**
     * Obtém o histórico de análises da fase de validação de um subprocesso.
     *
     * @param id O ID do subprocesso.
     * @return Uma {@link List} de {@link AnaliseValidacaoHistoricoDto} com o histórico.
     */
    @GetMapping("/{id}/historico-validacao")
    public List<AnaliseValidacaoHistoricoDto> obterHistoricoValidacao(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO)
                .stream()
                .map(analiseMapper::toAnaliseValidacaoHistoricoDto)
                .toList();
    }

    /**
     * Devolve a validação de um mapa para a unidade de negócio responsável para
     * que sejam feitos ajustes.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo a justificativa da devolução.
     * @param usuario O usuário autenticado que está realizando a devolução.
     */
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

    /**
     * Aceita a validação de um mapa, movendo o subprocesso para a próxima etapa
     * de análise hierárquica.
     *
     * @param id      O ID do subprocesso.
     * @param usuario O usuário autenticado que está aceitando a validação.
     */
    @PostMapping("/{id}/aceitar-validacao")
    @Transactional
    @Operation(summary = "Aceita a validação do mapa")
    public void aceitarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.aceitarValidacao(id, usuario);
    }

    /**
     * Homologa a validação de um mapa, finalizando o fluxo de aprovações.
     * <p>
     * Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @param id      O ID do subprocesso.
     * @param usuario O usuário autenticado (administrador) que realiza a homologação.
     */
    @PostMapping("/{id}/homologar-validacao")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa a validação do mapa")
    public void homologarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.homologarValidacao(id);
    }

    /**
     * Submete a versão ajustada de um mapa para uma nova rodada de validação.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo as observações e a nova data limite da etapa.
     * @param usuario O usuário autenticado que está submetendo os ajustes.
     */
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
