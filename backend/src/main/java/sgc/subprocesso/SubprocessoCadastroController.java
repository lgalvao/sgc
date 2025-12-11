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
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.AnaliseMapper;
import sgc.analise.model.TipoAnalise;
import sgc.atividade.model.Atividade;
import sgc.comum.erros.ErroValidacao;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoMapaService;
import sgc.subprocesso.service.SubprocessoService;
import sgc.subprocesso.service.SubprocessoWorkflowService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(
        name = "Subprocessos",
        description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCadastroController {

    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder().toFactory();

    private final SubprocessoService subprocessoService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final SubprocessoWorkflowService subprocessoWorkflowService;
    private final sgc.analise.AnaliseService analiseService;
    private final AnaliseMapper analiseMapper;

    private final SubprocessoMapaService subprocessoMapaService;
    private final SgrhService sgrhService;

    /**
     * Obtém o histórico de análises da fase de cadastro de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Uma {@link List} de {@link AnaliseHistoricoDto} com o histórico.
     */
    @GetMapping("/{codigo}/historico-cadastro")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long codigo) {
        return analiseService.listarPorSubprocesso(codigo, TipoAnalise.CADASTRO).stream()
                .map(analiseMapper::toAnaliseHistoricoDto)
                .toList();
    }

    /**
     * Disponibiliza o cadastro de atividades de um subprocesso para a próxima etapa de análise.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     */
    @PostMapping("/{codigo}/cadastro/disponibilizar")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<RespostaDto> disponibilizarCadastro(
            @PathVariable("codigo") Long codSubprocesso,
            @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        List<Atividade> faltando =
                subprocessoService.obterAtividadesSemConhecimento(codSubprocesso);
        if (faltando != null && !faltando.isEmpty()) {
            var lista =
                    faltando.stream()
                            .map(
                                    a ->
                                            Map.of(
                                                    "codigo",
                                                    a.getCodigo(),
                                                    "descricao",
                                                    a.getDescricao()))
                            .toList();
            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoWorkflowService.disponibilizarCadastro(codSubprocesso, usuario);
        return ResponseEntity.ok(new RespostaDto("Cadastro de atividades disponibilizado"));
    }

    /**
     * Disponibiliza a revisão do cadastro de atividades para a próxima etapa de análise.
     *
     * <p>Antes de disponibilizar, o método valida se todas as atividades do subprocesso possuem
     * pelo menos um conhecimento associado.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     * @throws ErroValidacao se existirem atividades sem conhecimentos.
     */
    @PostMapping("/{codigo}/disponibilizar-revisao")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<RespostaDto> disponibilizarRevisao(
            @PathVariable Long codigo, @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(codigo);
        if (faltando != null && !faltando.isEmpty()) {
            var lista =
                    faltando.stream()
                            .map(
                                    a ->
                                            Map.of(
                                                    "codigo",
                                                    a.getCodigo(),
                                                    "descricao",
                                                    a.getDescricao()))
                            .toList();
            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoWorkflowService.disponibilizarRevisao(codigo, usuario);
        return ResponseEntity.ok(
                new RespostaDto("Revisão do cadastro de atividades disponibilizada"));
    }

    /**
     * Obtém os dados de cadastro de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link SubprocessoCadastroDto} com os dados do cadastro.
     */
    @GetMapping("/{codigo}/cadastro")
    public SubprocessoCadastroDto obterCadastro(@PathVariable Long codigo) {
        return subprocessoDtoService.obterCadastro(codigo);
    }

    /**
     * Devolve o cadastro de um subprocesso para o responsável pela unidade para que sejam feitos
     * ajustes.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo o motivo e as observações da devolução.
     */
    @PostMapping("/{codigo}/devolver-cadastro")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.getObservacoes());

        subprocessoWorkflowService.devolverCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Aceita o cadastro de um subprocesso, movendo-o para a próxima etapa do fluxo de trabalho.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da aceitação.
     */
    @PostMapping("/{codigo}/aceitar-cadastro")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.getObservacoes());

        subprocessoWorkflowService.aceitarCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Homologa o cadastro de um subprocesso.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da homologação.
     */
    @PostMapping("/{codigo}/homologar-cadastro")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.getObservacoes());

        subprocessoWorkflowService.homologarCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Devolve a revisão de um cadastro de subprocesso para o responsável pela unidade para que
     * sejam feitos ajustes.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo o motivo e as observações da devolução.
     */
    @PostMapping("/{codigo}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public void devolverRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.getObservacoes());

        subprocessoWorkflowService.devolverRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Aceita a revisão do cadastro de um subprocesso.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da aceitação.
     */
    @PostMapping("/{codigo}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.getObservacoes());

        subprocessoWorkflowService.aceitarRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Homologa a revisão do cadastro de um subprocesso.
     *
     * <p>Esta ação é restrita a usuários com o perfil 'ADMIN'.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da homologação.
     */
    @PostMapping("/{codigo}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasRole('ADMIN')")
    public void homologarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Object principal) {
        String tituloUsuario = extractTituloUsuario(principal);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(tituloUsuario);
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.getObservacoes());

        subprocessoWorkflowService.homologarRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Importa atividades de um subprocesso de origem para o subprocesso de destino.
     *
     * @param codigo  O código do subprocesso de destino.
     * @param request O DTO contendo o código do subprocesso de origem.
     * @return Um {@link Map} com uma mensagem de sucesso.
     */
    @PostMapping("/{codigo}/importar-atividades")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long codigo, @RequestBody @Valid ImportarAtividadesReq request) {
        subprocessoMapaService.importarAtividades(codigo, request.getCodSubprocessoOrigem());
        return Map.of("message", "Atividades importadas.");
    }

    private String extractTituloUsuario(Object principal) {
        if (principal instanceof String) return (String) principal;
        if (principal instanceof sgc.sgrh.model.Usuario)
            return ((sgc.sgrh.model.Usuario) principal).getTituloEleitoral();
        return principal != null ? principal.toString() : null;
    }
}
