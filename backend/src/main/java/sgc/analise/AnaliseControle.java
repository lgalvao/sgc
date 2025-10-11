package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseValidacao;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos/{id}")
@RequiredArgsConstructor
public class AnaliseControle {
    private final AnaliseCadastroService analiseCadastroService;
    private final AnaliseValidacaoService analiseValidacaoService;

    @GetMapping("/analises-cadastro")
    public List<AnaliseCadastro> listarAnalisesCadastro(@PathVariable("id") Long id) {
        return analiseCadastroService.listarPorSubprocesso(id);
    }

    @PostMapping("/analises-cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AnaliseCadastro criarAnaliseCadastro(@PathVariable("id") Long id,
                                                 @RequestBody Map<String, String> payload) {
        String observacoes = payload != null ? payload.getOrDefault("observacoes", "") : "";
        return analiseCadastroService.criarAnalise(id, observacoes);
    }

    @GetMapping("/analises-validacao")
    public List<AnaliseValidacao> listarAnalisesValidacao(@PathVariable("id") Long id) {
        return analiseValidacaoService.listarPorSubprocesso(id);
    }

    @PostMapping("/analises-validacao")
    @ResponseStatus(HttpStatus.CREATED)
    public AnaliseValidacao criarAnaliseValidacao(@PathVariable("id") Long id,
                                                  @RequestBody Map<String, String> payload) {
        String observacoes = payload != null ? payload.getOrDefault("observacoes", "") : "";
        return analiseValidacaoService.criarAnalise(id, observacoes);
    }
}