package sgc.organizacao;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;

import java.util.*;

import static sgc.processo.model.TipoProcesso.*;

@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class UnidadeController {
    private final UnidadeService unidadeService;
    private final UnidadeHierarquiaService hierarquiaService;
    private final ResponsavelUnidadeService responsavelService;
    private final UsuarioService usuarioService;
    private final ProcessoService processoService;

    @PostMapping("/{codUnidade}/atribuicoes-temporarias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> criarAtribuicaoTemporaria(
            @PathVariable Long codUnidade, @Valid @RequestBody CriarAtribuicaoRequest request) {

        responsavelService.criarAtribuicaoTemporaria(codUnidade, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/atribuicoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AtribuicaoDto>> buscarTodasAtribuicoes() {
        return ResponseEntity.ok(responsavelService.buscarTodasAtribuicoes());
    }

    @GetMapping
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<List<UnidadeDto>> buscarTodasUnidades() {
        List<UnidadeDto> hierarquia = hierarquiaService.buscarArvoreHierarquica();
        return ResponseEntity.ok(hierarquia);
    }

    @GetMapping("/arvore-com-elegibilidade")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<List<UnidadeDto>> buscarArvoreComElegibilidade(
            @RequestParam("tipoProcesso") String tipoProcesso,
            @RequestParam(value = "codProcesso", required = false) Long codProcesso) {

        TipoProcesso tipo = TipoProcesso.valueOf(tipoProcesso);
        boolean requerMapaVigente = tipo == REVISAO || tipo == DIAGNOSTICO;

        Set<Long> bloqueadas = processoService.buscarIdsUnidadesComProcessosAtivos(codProcesso);
        List<UnidadeDto> arvore = hierarquiaService.buscarArvoreComElegibilidade(requerMapaVigente, bloqueadas);

        return ResponseEntity.ok(arvore);
    }

    @GetMapping("/{codUnidade}/mapa-vigente")
    public ResponseEntity<Map<String, Boolean>> verificarMapaVigente(@PathVariable Long codUnidade) {
        boolean temMapaVigente = unidadeService.verificarMapaVigente(codUnidade);
        return ResponseEntity.ok(Map.of("temMapaVigente", temMapaVigente));
    }

    @GetMapping("/{codUnidade}/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<List<UsuarioConsultaDto>> buscarUsuariosPorUnidade(@PathVariable Long codUnidade) {
        List<UsuarioConsultaDto> usuarios = usuarioService.buscarPorUnidadeLotacao(codUnidade).stream()
                .map(UsuarioConsultaDto::fromEntity)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/sigla/{siglaUnidade}")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<UnidadeDto> buscarUnidadePorSigla(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_.-]+$") String siglaUnidade) {
        Unidade unidade = unidadeService.buscarPorSigla(siglaUnidade);
        return ResponseEntity.ok(UnidadeDto.fromEntity(unidade));
    }

    @GetMapping("/{codigo}")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<UnidadeDto> buscarUnidadePorCodigo(@PathVariable Long codigo) {
        Unidade unidade = unidadeService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(UnidadeDto.fromEntity(unidade));
    }

    @GetMapping("/{codigo}/arvore")
    @JsonView(OrganizacaoViews.Publica.class)
    public ResponseEntity<UnidadeDto> buscarArvoreUnidade(@PathVariable Long codigo) {
        return ResponseEntity.ok(hierarquiaService.buscarArvore(codigo));
    }

    @GetMapping("/sigla/{sigla}/subordinadas")
    public ResponseEntity<List<String>> buscarSiglasSubordinadas(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_.-]+$") String sigla) {
        List<String> siglas = hierarquiaService.buscarSiglasSubordinadas(sigla);
        return ResponseEntity.ok(siglas);
    }

    @GetMapping("/sigla/{sigla}/superior")
    public ResponseEntity<String> buscarSiglaSuperior(@PathVariable @Pattern(regexp = "^[a-zA-Z0-9_.-]+$") String sigla) {
        return hierarquiaService
                .buscarSiglaSuperior(sigla)
                .map(res -> ResponseEntity.ok(HtmlUtils.htmlEscape(res)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
