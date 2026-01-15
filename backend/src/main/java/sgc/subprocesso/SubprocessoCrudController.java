package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoFacade;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCrudController {
    private final SubprocessoFacade subprocessoFacade;
    private final UnidadeService unidadeService;

    /**
     * Obtém as permissões do usuário para um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return DTO com as permissões calculadas.
     */
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

    /**
     * Lista todos os subprocessos.
     * <p>Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @return Uma {@link List} de {@link SubprocessoDto}.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SubprocessoDto> listar() {
        return subprocessoFacade.listar();
    }

    /**
     * Obtém os detalhes de um subprocesso específico.
     *
     * @param codigo         O código do subprocesso.
     * @param perfil         O perfil do usuário que faz a requisição (opcional).
     * @param unidadeUsuario O código da unidade do usuário (opcional).
     * @return Um {@link SubprocessoDetalheDto} com os detalhes do subprocesso.
     */
    @GetMapping("/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public SubprocessoDetalheDto obterPorCodigo(
            @PathVariable Long codigo,
            @RequestParam(required = false) sgc.organizacao.model.Perfil perfil,
            @RequestParam(required = false) Long unidadeUsuario) {
        return subprocessoFacade.obterDetalhes(codigo, null);
    }

    /**
     * Busca um subprocesso por código do processo e sigla da unidade.
     *
     * @param codProcesso  O código do processo.
     * @param siglaUnidade A sigla da unidade.
     * @return O {@link SubprocessoDto} encontrado.
     */
    @GetMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubprocessoDto> buscarPorProcessoEUnidade(
            @RequestParam Long codProcesso, @RequestParam String siglaUnidade) {
        UnidadeDto unidade = unidadeService.buscarPorSigla(siglaUnidade);
        SubprocessoDto dto =
                subprocessoFacade.obterPorProcessoEUnidade(codProcesso, unidade.getCodigo());
        return ResponseEntity.ok(dto);
    }

    /**
     * Cria um novo subprocesso.
     *
     * <p>Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @param request O DTO com os dados do subprocesso a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo subprocesso e o
     * {@link SubprocessoDto} criado no corpo da resposta.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubprocessoDto> criar(@Valid @RequestBody CriarSubprocessoRequest request) {
        var salvo = subprocessoFacade.criar(request);
        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    /**
     * Atualiza um subprocesso existente.
     *
     * <p>Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @param codigo O código do subprocesso a ser atualizado.
     * @param request O DTO com os novos dados do subprocesso.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link SubprocessoDto} atualizado.
     */
    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubprocessoDto> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody AtualizarSubprocessoRequest request) {
        var atualizado = subprocessoFacade.atualizar(codigo, request);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Exclui um subprocesso.
     *
     * <p>Ação restrita a usuários com perfil 'ADMIN'.
     *
     * @param codigo O código do subprocesso a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codigo}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        subprocessoFacade.excluir(codigo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Altera a data limite de um subprocesso.
     * (CDU-27)
     */
    @PostMapping("/{codigo}/data-limite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarDataLimite(
            @PathVariable Long codigo,
            @RequestBody @Valid AlterarDataLimiteRequest request) {
        subprocessoFacade.alterarDataLimite(codigo, request.getNovaDataLimite());
        return ResponseEntity.ok().build();
    }

    /**
     * Reabre o cadastro de um subprocesso.
     * (CDU-32)
     */
    @PostMapping("/{codigo}/reabrir-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre o cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirCadastro(
            @PathVariable Long codigo,
            @RequestBody @Valid ReabrirProcessoRequest request) {
        subprocessoFacade.reabrirCadastro(codigo, request.getJustificativa());
        return ResponseEntity.ok().build();
    }

    /**
     * Reabre a revisão de cadastro de um subprocesso.
     * (CDU-33)
     */
    @PostMapping("/{codigo}/reabrir-revisao-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre a revisão de cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirRevisaoCadastro(
            @PathVariable Long codigo,
            @RequestBody @Valid ReabrirProcessoRequest request) {
        subprocessoFacade.reabrirRevisaoCadastro(codigo, request.getJustificativa());
        return ResponseEntity.ok().build();
    }
}
