package sgc.subprocesso;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.comum.dto.ComumDtos.DataRequest;
import sgc.comum.dto.ComumDtos.JustificativaRequest;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoViews;
import sgc.subprocesso.service.SubprocessoFacade;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCrudController {
    private final SubprocessoFacade subprocessoFacade;
    private final OrganizacaoFacade organizacaoFacade;

    @GetMapping("/{codigo}/permissoes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubprocessoPermissoesDto> obterPermissoes(@PathVariable Long codigo) {
        SubprocessoPermissoesDto permissoes = subprocessoFacade.obterPermissoes(codigo);
        return ResponseEntity.ok(permissoes);
    }

    @GetMapping("/{codigo}/validar-cadastro")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Valida se o cadastro está pronto para disponibilização")
    public ResponseEntity<ValidacaoCadastroDto> validarCadastro(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoFacade.validarCadastro(codigo));
    }

    @GetMapping("/{codigo}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém apenas o status atual do subprocesso")
    public ResponseEntity<SubprocessoSituacaoDto> obterStatus(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoFacade.obterSituacao(codigo));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public List<Subprocesso> listar() {
        return subprocessoFacade.listar();
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("isAuthenticated()")
    @JsonView(SubprocessoViews.Publica.class)
    public SubprocessoDetalheResponse obterPorCodigo(@PathVariable Long codigo) {
        return subprocessoFacade.obterDetalhes(codigo);
    }

    @GetMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> buscarPorProcessoEUnidade(
            @RequestParam Long codProcesso, @RequestParam String siglaUnidade) {
        UnidadeDto unidade = organizacaoFacade.buscarUnidadePorSigla(siglaUnidade);
        Subprocesso sp = subprocessoFacade.obterEntidadePorProcessoEUnidade(codProcesso, unidade.getCodigo());
        return ResponseEntity.ok(sp);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> criar(@Valid @RequestBody CriarSubprocessoRequest request) {
        var salvo = subprocessoFacade.criar(request);
        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody AtualizarSubprocessoRequest request) {
        var atualizado = subprocessoFacade.atualizar(codigo, request);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{codigo}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        subprocessoFacade.excluir(codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/data-limite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarDataLimite(
            @PathVariable Long codigo,
            @RequestBody @Valid DataRequest request) {
        subprocessoFacade.alterarDataLimite(codigo, request.data());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/reabrir-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre o cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirCadastro(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request) {
        subprocessoFacade.reabrirCadastro(codigo, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/reabrir-revisao-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre a revisão de cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirRevisaoCadastro(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request) {
        subprocessoFacade.reabrirRevisaoCadastro(codigo, request.justificativa());
        return ResponseEntity.ok().build();
    }
}
