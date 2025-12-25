package sgc.unidade.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.processo.api.model.TipoProcesso;
import sgc.sgrh.api.UsuarioDto;
import sgc.sgrh.api.UnidadeDto;
import sgc.unidade.api.AtribuicaoTemporariaDto;
import sgc.unidade.api.CriarAtribuicaoTemporariaReq;
import sgc.unidade.service.UnidadeService;

import java.util.List;
import java.util.Map;

/**
 * Controle para operações relacionadas a unidades organizacionais
 */
@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadeController {
    private final UnidadeService unidadeService;

    /**
     * Cria uma nova atribuição temporária para um usuário em uma unidade.
     *
     * @param codUnidade O Código da unidade.
     * @param request    Os dados da atribuição.
     * @return Resposta vazia com status 201 (Created).
     */
    @PostMapping("/{codUnidade}/atribuicoes-temporarias")
    public ResponseEntity<Void> criarAtribuicaoTemporaria(
            @PathVariable Long codUnidade, @RequestBody CriarAtribuicaoTemporariaReq request) {
        unidadeService.criarAtribuicaoTemporaria(codUnidade, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Busca todas as atribuições temporárias.
     *
     * @return Lista de atribuições temporárias.
     */
    @GetMapping("/atribuicoes")
    public ResponseEntity<List<AtribuicaoTemporariaDto>> buscarTodasAtribuicoes() {
        return ResponseEntity.ok(unidadeService.buscarTodasAtribuicoes());
    }

    /**
     * Busca todas as unidades em estrutura hierárquica Endpoint usado pela tela de cadastro de
     * processo para selecionar unidades participantes
     */
    @GetMapping
    public ResponseEntity<List<UnidadeDto>> buscarTodasUnidades() {
        List<UnidadeDto> hierarquia = unidadeService.buscarTodasUnidades();
        return ResponseEntity.ok(hierarquia);
    }

    /**
     * Busca a árvore de unidades indicando a elegibilidade de cada uma para participar de um
     * processo.
     *
     * @param tipoProcesso O tipo de processo a ser criado.
     * @param codProcesso  O código do processo (opcional, para edição).
     * @return A lista de unidades raiz com a árvore de filhas e a elegibilidade.
     */
    @GetMapping("/arvore-com-elegibilidade")
    public ResponseEntity<List<UnidadeDto>> buscarArvoreComElegibilidade(
            @RequestParam("tipoProcesso") String tipoProcesso,
            @RequestParam(value = "codProcesso", required = false) Long codProcesso) {
        List<UnidadeDto> arvore =
                unidadeService.buscarArvoreComElegibilidade(
                        TipoProcesso.valueOf(tipoProcesso), codProcesso);
        return ResponseEntity.ok(arvore);
    }

    /**
     * Verifica se a unidade possui mapa de competências vigente Usado pelo frontend para determinar
     * se deve exibir opções de revisão
     *
     * @param codigoUnidade O código da unidade
     * @return Um objeto com o campo temMapaVigente (boolean)
     */
    @GetMapping("/{codigoUnidade}/mapa-vigente")
    public ResponseEntity<Map<String, Boolean>> verificarMapaVigente(
            @PathVariable Long codigoUnidade) {
        boolean temMapaVigente = unidadeService.verificarMapaVigente(codigoUnidade);
        return ResponseEntity.ok(Map.of("temMapaVigente", temMapaVigente));
    }

    /**
     * Busca usuários de uma unidade específica.
     *
     * @param codigoUnidade O código da unidade
     * @return Lista de usuários da unidade
     */
    @GetMapping("/{codigoUnidade}/usuarios")
    public ResponseEntity<List<UsuarioDto>> buscarUsuariosPorUnidade(
            @PathVariable Long codigoUnidade) {
        List<UsuarioDto> usuarios = unidadeService.buscarUsuariosPorUnidade(codigoUnidade);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca uma unidade específica pela sua sigla.
     *
     * @param sigla A sigla da unidade.
     * @return Os dados da unidade.
     */
    @GetMapping("/sigla/{sigla}")
    public ResponseEntity<UnidadeDto> buscarUnidadePorSigla(@PathVariable String sigla) {
        UnidadeDto unidade = unidadeService.buscarPorSigla(sigla);
        return ResponseEntity.ok(unidade);
    }

    /**
     * Busca uma unidade específica pelo seu código.
     *
     * @param codigo O código da unidade.
     * @return Os dados da unidade.
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<UnidadeDto> buscarUnidadePorCodigo(@PathVariable Long codigo) {
        UnidadeDto unidade = unidadeService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(unidade);
    }

    /**
     * Busca a árvore de uma unidade específica (incluindo subunidades).
     *
     * @param codigo O código da unidade.
     * @return A unidade com sua árvore de subunidades.
     */
    @GetMapping("/{codigo}/arvore")
    public ResponseEntity<UnidadeDto> buscarArvoreUnidade(@PathVariable Long codigo) {
        UnidadeDto unidade = unidadeService.buscarArvore(codigo);
        if (unidade == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(unidade);
    }

    /**
     * Busca as siglas de todas as unidades subordinadas (diretas e indiretas) e a própria unidade.
     *
     * @param sigla A sigla da unidade raiz.
     * @return Lista de siglas.
     */
    @GetMapping("/sigla/{sigla}/subordinadas")
    public ResponseEntity<List<String>> buscarSiglasSubordinadas(@PathVariable String sigla) {
        List<String> siglas = unidadeService.buscarSiglasSubordinadas(sigla);
        return ResponseEntity.ok(siglas);
    }

    /**
     * Busca a sigla da unidade superior imediata.
     *
     * @param sigla A sigla da unidade.
     * @return A sigla da unidade superior ou 204 se não houver.
     */
    @GetMapping("/sigla/{sigla}/superior")
    public ResponseEntity<String> buscarSiglaSuperior(@PathVariable String sigla) {
        String siglaSuperior = unidadeService.buscarSiglaSuperior(sigla);
        if (siglaSuperior == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(siglaSuperior);
    }
}
