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
import sgc.analise.modelo.TipoAnalise;
import sgc.atividade.modelo.Atividade;
import sgc.comum.erros.ErroValidacao;
import sgc.sgrh.Usuario;
import sgc.subprocesso.dto.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCadastroControle {

    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private final SubprocessoService subprocessoService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final SubprocessoWorkflowService subprocessoWorkflowService;
    private final sgc.analise.AnaliseService analiseService;
    private final AnaliseMapper analiseMapper;
    private final SubprocessoMapaService subprocessoMapaService;

    /**
     * Obtém o histórico de análises da fase de cadastro de um subprocesso.
     *
     * @param id O ID do subprocesso.
     * @return Uma {@link List} de {@link AnaliseHistoricoDto} com o histórico.
     */
    @GetMapping("/{id}/historico-cadastro")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.CADASTRO)
                .stream()
                .map(analiseMapper::toAnaliseHistoricoDto)
                .toList();
    }

    /**
     * Disponibiliza o cadastro de atividades de um subprocesso para a próxima
     * etapa de análise.
     *
     * @param subprocessoId O ID do subprocesso.
     * @param usuario       O usuário autenticado que está realizando a ação.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     */
    @PostMapping("/{id}/disponibilizar")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<RespostaDto> disponibilizarCadastro(
            @PathVariable("id") Long subprocessoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoWorkflowService.disponibilizarCadastro(subprocessoId, usuario);
        return ResponseEntity.ok(new RespostaDto("Cadastro de atividades disponibilizado"));
    }

    /**
     * Disponibiliza a revisão do cadastro de atividades para a próxima etapa de análise.
     * <p>
     * Antes de disponibilizar, o método valida se todas as atividades do subprocesso
     * possuem pelo menos um conhecimento associado.
     *
     * @param id      O ID do subprocesso.
     * @param usuario O usuário autenticado que está realizando a ação.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     * @throws ErroValidacao se existirem atividades sem conhecimentos.
     */
    @PostMapping("/{id}/disponibilizar-revisao")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<RespostaDto> disponibilizarRevisao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(id);
        if (faltando != null && !faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("id", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.", Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoWorkflowService.disponibilizarRevisao(id, usuario);
        return ResponseEntity.ok(new RespostaDto("Revisão do cadastro de atividades disponibilizada"));
    }

    /**
     * Obtém os dados de cadastro de um subprocesso.
     *
     * @param id O ID do subprocesso.
     * @return Um {@link SubprocessoCadastroDto} com os dados do cadastro.
     */
    @GetMapping("/{id}/cadastro")
    public SubprocessoCadastroDto obterCadastro(@PathVariable Long id) {
        return subprocessoDtoService.obterCadastro(id);
    }

    /**
     * Devolve o cadastro de um subprocesso para o responsável pela unidade para
     * que sejam feitos ajustes.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo o motivo e as observações da devolução.
     * @param usuario O usuário autenticado (analista) que está realizando a devolução.
     */
    @PostMapping("/{id}/devolver-cadastro")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedMotivo = HTML_SANITIZER_POLICY.sanitize(request.motivo());
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.devolverCadastro(
                id,
                sanitizedMotivo,
                sanitizedObservacoes,
                usuario
        );
    }

    /**
     * Aceita o cadastro de um subprocesso, movendo-o para a próxima etapa do fluxo
     * de trabalho.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo as observações da aceitação.
     * @param usuario O usuário autenticado (analista) que está aceitando o cadastro.
     */
    @PostMapping("/{id}/aceitar-cadastro")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.aceitarCadastro(
                id,
                sanitizedObservacoes,
                usuario.getTituloEleitoral()
        );
    }

    /**
     * Homologa o cadastro de um subprocesso.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo as observações da homologação.
     * @param usuario O usuário autenticado (gestor) que está homologando o cadastro.
     */
    @PostMapping("/{id}/homologar-cadastro")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.homologarCadastro(
                id,
                sanitizedObservacoes,
                usuario.getTituloEleitoral()
        );
    }

    /**
     * Devolve a revisão de um cadastro de subprocesso para o responsável pela unidade
     * para que sejam feitos ajustes.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo o motivo e as observações da devolução.
     * @param usuario O usuário autenticado (analista) que está realizando a devolução.
     */
    @PostMapping("/{id}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    public void devolverRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedMotivo = HTML_SANITIZER_POLICY.sanitize(request.motivo());
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.devolverRevisaoCadastro(
                id,
                sanitizedMotivo,
                sanitizedObservacoes,
                usuario
        );
    }

    /**
     * Aceita a revisão do cadastro de um subprocesso.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo as observações da aceitação.
     * @param usuario O usuário autenticado (analista) que está aceitando a revisão.
     */
    @PostMapping("/{id}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    public void aceitarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.aceitarRevisaoCadastro(
                id,
                sanitizedObservacoes,
                usuario
        );
    }

    /**
     * Homologa a revisão do cadastro de um subprocesso.
     * <p>
     * Esta ação é restrita a usuários com o perfil 'ADMIN'.
     *
     * @param id      O ID do subprocesso.
     * @param request O DTO contendo as observações da homologação.
     * @param usuario O usuário autenticado (administrador) que está homologando.
     */
    @PostMapping("/{id}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasRole('ADMIN')")
    public void homologarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.homologarRevisaoCadastro(
                id,
                sanitizedObservacoes,
                usuario
        );
    }

    /**
     * Importa atividades de um subprocesso de origem para o subprocesso de destino.
     *
     * @param id      O ID do subprocesso de destino.
     * @param request O DTO contendo o ID do subprocesso de origem.
     * @return Um {@link Map} com uma mensagem de sucesso.
     */
    @PostMapping("/{id}/importar-atividades")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long id,
            @RequestBody @Valid ImportarAtividadesRequest request
    ) {
        subprocessoMapaService.importarAtividades(id, request.subprocessoOrigemId());
        return Map.of("message", "Atividades importadas com sucesso.");
    }
}
