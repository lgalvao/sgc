package sgc.atividade;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.ErroDominioNaoEncontrado;

import java.util.List;
import java.util.Map;

/**
 * Controller para expor endpoints de análise (cadastro e validação).
 * Todos os Metodos usam nomes e mensagens em português.
 */
@RestController
@RequestMapping("/api/subprocessos/{id}")
@RequiredArgsConstructor
public class AnaliseController {
    private final AnaliseCadastroService analiseCadastroService;
    private final AnaliseValidacaoService analiseValidacaoService;

    @GetMapping("/analises-cadastro")
    public ResponseEntity<?> listarAnalisesCadastro(@PathVariable("id") Long id) {
        try {
            List<AnaliseCadastro> lista = analiseCadastroService.listarPorSubprocesso(id);
            return ResponseEntity.ok(lista);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    @PostMapping("/analises-cadastro")
    public ResponseEntity<?> criarAnaliseCadastro(@PathVariable("id") Long id,
                                                 @RequestBody Map<String, String> payload) {
        try {
            String observacoes = payload != null ? payload.getOrDefault("observacoes", "") : "";
            AnaliseCadastro criado = analiseCadastroService.criarAnalise(id, observacoes);
            return ResponseEntity.status(201).body(criado);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    @GetMapping("/analises-validacao")
    public ResponseEntity<?> listarAnalisesValidacao(@PathVariable("id") Long id) {
        try {
            List<AnaliseValidacao> lista = analiseValidacaoService.listarPorSubprocesso(id);
            return ResponseEntity.ok(lista);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    @PostMapping("/analises-validacao")
    public ResponseEntity<?> criarAnaliseValidacao(@PathVariable("id") Long id,
                                                  @RequestBody Map<String, String> payload) {
        try {
            String observacoes = payload != null ? payload.getOrDefault("observacoes", "") : "";
            AnaliseValidacao criado = analiseValidacaoService.criarAnalise(id, observacoes);
            return ResponseEntity.status(201).body(criado);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

}