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

    @GetMapping("/{id}/historico-cadastro")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.CADASTRO)
                .stream()
                .map(analiseMapper::toAnaliseHistoricoDto)
                .toList();
    }

    @PostMapping("/{id}/disponibilizar")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<RespostaDto> disponibilizarCadastro(
            @PathVariable("id") Long subprocessoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoWorkflowService.disponibilizarCadastro(subprocessoId, usuario);
        return ResponseEntity.ok(new RespostaDto("Cadastro de atividades disponibilizado"));
    }

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

    @GetMapping("/{id}/cadastro")
    public SubprocessoCadastroDto obterCadastro(@PathVariable Long id) {
        return subprocessoDtoService.obterCadastro(id);
    }

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
