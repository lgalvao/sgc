package sgc.unidade;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.service.UnidadeService;

import java.util.List;
import java.util.Map;

/**
 * Controller para operações relacionadas a unidades organizacionais
 */
@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadeController {
    private final UnidadeService unidadeService;

    /**
     * Cria uma nova atribuição temporária para um servidor em uma unidade.
     * @param idUnidade O ID da unidade.
     * @param request Os dados da atribuição.
     * @return Resposta vazia com status 201 (Created).
     */
    @PostMapping("/{idUnidade}/atribuicoes-temporarias")
    public ResponseEntity<Void> criarAtribuicaoTemporaria(
        @PathVariable Long idUnidade,
        @RequestBody CriarAtribuicaoTemporariaRequest request
    ) {
        unidadeService.criarAtribuicaoTemporaria(idUnidade, request);
        return ResponseEntity.created(null).build();
    }

    /**
     * Busca todas as unidades em estrutura hierárquica
     * Endpoint usado pela tela de cadastro de processo para selecionar unidades participantes
     */
    @GetMapping
    public ResponseEntity<List<UnidadeDto>> buscarTodasUnidades() {
        List<UnidadeDto> hierarquia = unidadeService.buscarTodasUnidades();
        return ResponseEntity.ok(hierarquia);
    }
    
    /**
     * Verifica se a unidade possui mapa de competências vigente
     * Usado pelo frontend para determinar se deve exibir opções de revisão
     * 
     * @param codigoUnidade O código da unidade
     * @return Um objeto com o campo temMapaVigente (boolean)
     */
    @GetMapping("/{codigoUnidade}/mapa-vigente")
    public ResponseEntity<Map<String, Boolean>> verificarMapaVigente(@PathVariable Long codigoUnidade) {
        boolean temMapaVigente = unidadeService.verificarMapaVigente(codigoUnidade);
        return ResponseEntity.ok(Map.of("temMapaVigente", temMapaVigente));
    }
    
    /**
     * Busca servidores (usuários) de uma unidade específica
     * Usado pelo frontend para validar se unidade tem servidores em processos de diagnóstico
     * 
     * @param codigoUnidade O código da unidade
     * @return Lista de servidores da unidade
     */
    @GetMapping("/{codigoUnidade}/servidores")
    public ResponseEntity<List<ServidorDto>> buscarServidoresPorUnidade(@PathVariable Long codigoUnidade) {
        List<ServidorDto> servidores = unidadeService.buscarServidoresPorUnidade(codigoUnidade);
        return ResponseEntity.ok(servidores);
    }

    /**
     * Busca usuários de uma unidade específica
     * Endpoint padronizado para o novo padrão de nomenclatura
     * 
     * @param codigoUnidade O código da unidade
     * @return Lista de usuários da unidade
     */
    @GetMapping("/{codigoUnidade}/usuarios")
    public ResponseEntity<List<ServidorDto>> buscarUsuariosPorUnidade(@PathVariable Long codigoUnidade) {
        List<ServidorDto> usuarios = unidadeService.buscarServidoresPorUnidade(codigoUnidade);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca uma unidade específica pela sua sigla.
     * @param sigla A sigla da unidade.
     * @return Os dados da unidade.
     */
    @GetMapping("/sigla/{sigla}")
    public ResponseEntity<UnidadeDto> buscarUnidadePorSigla(@PathVariable String sigla) {
        UnidadeDto unidade = unidadeService.buscarPorSigla(sigla);
        return ResponseEntity.ok(unidade);
    }
}
