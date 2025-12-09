package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.sgrh.dto.UnidadeDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.dto.SubprocessoStatusDto;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoService;
import sgc.unidade.service.UnidadeService;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(
        name = "Subprocessos",
        description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCrudController {
    private final SubprocessoService subprocessoService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final UnidadeService unidadeService;

    /**
     * Obtém as permissões do usuário para um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return DTO com as permissões calculadas.
     */
    @GetMapping("/{codigo}/permissoes")
    public ResponseEntity<SubprocessoPermissoesDto> obterPermissoes(@PathVariable Long codigo) {
        SubprocessoPermissoesDto permissoes = subprocessoDtoService.obterPermissoes(codigo);
        return ResponseEntity.ok(permissoes);
    }

    @GetMapping("/{codigo}/validar-cadastro")
    @Operation(summary = "Valida se o cadastro está pronto para disponibilização")
    public ResponseEntity<ValidacaoCadastroDto> validarCadastro(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoDtoService.validarCadastro(codigo));
    }

    @GetMapping("/{codigo}/status")
    @Operation(summary = "Obtém apenas o status atual do subprocesso")
    public ResponseEntity<SubprocessoStatusDto> obterStatus(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoDtoService.obterStatus(codigo));
    }

    /**
     * Lista todos os subprocessos.
     *
     * @return Uma {@link List} de {@link SubprocessoDto}.
     */
    @GetMapping
    public List<SubprocessoDto> listar() {
        return subprocessoDtoService.listar();
    }

    /**
     * Obtém os detalhes de um subprocesso específico.
     *
     * @param codigo O código do subprocesso.
     * @param perfil O perfil do usuário que faz a requisição (opcional).
     * @param unidadeUsuario O código da unidade do usuário (opcional).
     * @return Um {@link SubprocessoDetalheDto} com os detalhes do subprocesso.
     */
    @GetMapping("/{codigo}")
    public SubprocessoDetalheDto obterPorCodigo(
            @PathVariable Long codigo,
            @RequestParam(required = false) sgc.sgrh.model.Perfil perfil,
            @RequestParam(required = false) Long unidadeUsuario) {
        return subprocessoDtoService.obterDetalhes(codigo, perfil, unidadeUsuario);
    }

    /**
     * Busca um subprocesso por código do processo e sigla da unidade.
     *
     * @param codProcesso O código do processo.
     * @param siglaUnidade A sigla da unidade.
     * @return O {@link SubprocessoDto} encontrado.
     */
    @GetMapping("/buscar")
    public ResponseEntity<SubprocessoDto> buscarPorProcessoEUnidade(
            @RequestParam Long codProcesso, @RequestParam String siglaUnidade) {
        UnidadeDto unidade = unidadeService.buscarPorSigla(siglaUnidade);
        SubprocessoDto dto =
                subprocessoDtoService.obterPorProcessoEUnidade(codProcesso, unidade.getCodigo());
        return ResponseEntity.ok(dto);
    }

    /**
     * Cria um novo subprocesso.
     *
     * @param subprocessoDto O DTO com os dados do subprocesso a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo subprocesso e o
     *     {@link SubprocessoDto} criado no corpo da resposta.
     */
    @PostMapping
    public ResponseEntity<SubprocessoDto> criar(@Valid @RequestBody SubprocessoDto subprocessoDto) {
        var salvo = subprocessoService.criar(subprocessoDto);
        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    /**
     * Atualiza um subprocesso existente.
     *
     * @param codigo O código do subprocesso a ser atualizado.
     * @param subprocessoDto O DTO com os novos dados do subprocesso.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link SubprocessoDto} atualizado.
     */
    @PostMapping("/{codigo}/atualizar")
    public ResponseEntity<SubprocessoDto> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody SubprocessoDto subprocessoDto) {
        var atualizado = subprocessoService.atualizar(codigo, subprocessoDto);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Exclui um subprocesso.
     *
     * @param codigo O código do subprocesso a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codigo}/excluir")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        subprocessoService.excluir(codigo);
        return ResponseEntity.noContent().build();
    }
}
