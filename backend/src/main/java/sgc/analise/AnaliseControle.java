package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.analise.modelo.TipoAnalise;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos/{id}")
@RequiredArgsConstructor
public class AnaliseControle {
    private final AnaliseService analiseService;

    @ExceptionHandler(ErroEntidadeNaoEncontrada.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleErroEntidadeNaoEncontrada(ErroEntidadeNaoEncontrada ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Map.of("message", "A requisição contém um argumento inválido ou malformado.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGenericException(Exception ex) {
        return Map.of("message", "Ocorreu um erro inesperado. Contate o suporte.");
    }


    @GetMapping("/analises-cadastro")
    public List<Analise> listarAnalisesCadastro(@PathVariable("id") Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.CADASTRO);
    }

    @PostMapping("/analises-cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public Analise criarAnaliseCadastro(@PathVariable("id") Long id,
                                                 @RequestBody Map<String, String> payload) {
        String observacoes = payload != null ? payload.getOrDefault("observacoes", "") : "";
        return analiseService.criarAnalise(id, observacoes, TipoAnalise.CADASTRO, null, payload.get("unidadeSigla"), payload.get("analistaUsuarioTitulo"), payload.get("motivo"));
    }

    @GetMapping("/analises-validacao")
    public List<Analise> listarAnalisesValidacao(@PathVariable("id") Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO);
    }

    @PostMapping("/analises-validacao")
    @ResponseStatus(HttpStatus.CREATED)
    public Analise criarAnaliseValidacao(@PathVariable("id") Long id,
                                                  @RequestBody Map<String, String> payload) {
        String observacoes = payload != null ? payload.getOrDefault("observacoes", "") : "";
        return analiseService.criarAnalise(id, observacoes, TipoAnalise.VALIDACAO, null, payload.get("unidadeSigla"), payload.get("analistaUsuarioTitulo"), payload.get("motivo"));
    }
}
