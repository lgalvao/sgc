package sgc.organizacao;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import sgc.organizacao.dto.AtribuicaoDto;
import sgc.organizacao.dto.CriarAtribuicaoRequest;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.OrganizacaoViews;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import sgc.processo.ProcessoFacade;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static sgc.processo.model.TipoProcesso.DIAGNOSTICO;
import static sgc.processo.model.TipoProcesso.REVISAO;

@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
@Validated
public class UnidadeController {
    private final OrganizacaoFacade organizacaoFacade;
    private final ProcessoFacade processoFacade;

    @PostMapping("/{codUnidade}/atribuicoes-temporarias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> criarAtribuicaoTemporaria(
            @PathVariable Long codUnidade, @Valid @RequestBody CriarAtribuicaoRequest request) {

        organizacaoFacade.criarAtribuicaoTemporaria(codUnidade, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/atribuicoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AtribuicaoDto>> buscarTodasAtribuicoes() {
        return ResponseEntity.ok(organizacaoFacade.buscarTodasAtribuicoes());
    }

    @GetMapping
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<List<UnidadeDto>> buscarTodasUnidades() {
        List<UnidadeDto> hierarquia = organizacaoFacade.buscarTodasUnidades();
        return ResponseEntity.ok(hierarquia);
    }

    @GetMapping("/arvore-com-elegibilidade")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<List<UnidadeDto>> buscarArvoreComElegibilidade(
            @RequestParam("tipoProcesso") String tipoProcesso,
            @RequestParam(value = "codProcesso", required = false) Long codProcesso) {

        TipoProcesso tipo = TipoProcesso.valueOf(tipoProcesso);
        boolean requerMapaVigente = tipo == REVISAO || tipo == DIAGNOSTICO;

        Set<Long> bloqueadas = processoFacade.buscarIdsUnidadesEmProcessosAtivos(codProcesso);
        List<UnidadeDto> arvore = organizacaoFacade.buscarArvoreComElegibilidade(requerMapaVigente, bloqueadas);

        return ResponseEntity.ok(arvore);
    }

    @GetMapping("/{codUnidade}/mapa-vigente")
    public ResponseEntity<Map<String, Boolean>> verificarMapaVigente(@PathVariable Long codUnidade) {
        boolean temMapaVigente = organizacaoFacade.verificarMapaVigente(codUnidade);
        return ResponseEntity.ok(Map.of("temMapaVigente", temMapaVigente));
    }

    @GetMapping("/{codUnidade}/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<List<Usuario>> buscarUsuariosPorUnidade(@PathVariable Long codUnidade) {
        List<Usuario> usuarios = organizacaoFacade.usuariosPorCodigoUnidade(codUnidade);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/sigla/{siglaUnidade}")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<UnidadeDto> buscarUnidadePorSigla(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_.-]+$") String siglaUnidade) {
        UnidadeDto unidade = organizacaoFacade.buscarPorSigla(siglaUnidade);
        return ResponseEntity.ok(unidade);
    }

    @GetMapping("/{codigo}")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<UnidadeDto> buscarUnidadePorCodigo(@PathVariable Long codigo) {
        UnidadeDto unidade = organizacaoFacade.dtoPorCodigo(codigo);
        return ResponseEntity.ok(unidade);
    }

    @GetMapping("/{codigo}/arvore")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<UnidadeDto> buscarArvoreUnidade(@PathVariable Long codigo) {
        return ResponseEntity.ok(organizacaoFacade.buscarArvore(codigo));
    }

    @GetMapping("/sigla/{sigla}/subordinadas")
    public ResponseEntity<List<String>> buscarSiglasSubordinadas(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_.-]+$") String sigla) {
        List<String> siglas = organizacaoFacade.buscarSiglasSubordinadas(sigla);
        return ResponseEntity.ok(siglas);
    }

    @GetMapping("/sigla/{sigla}/superior")
    public ResponseEntity<String> buscarSiglaSuperior(@PathVariable @Pattern(regexp = "^[a-zA-Z0-9_.-]+$") String sigla) {
        return organizacaoFacade
                .buscarSiglaSuperior(sigla)
                .map(res -> ResponseEntity.ok(HtmlUtils.htmlEscape(res)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
