package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sgc.analise.dto.CriarAnaliseRequestDto;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.TipoAnalise;

import sgc.comum.erros.ErroDominioNaoEncontrado;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos/{id}")
@RequiredArgsConstructor
public class AnaliseControle {
    private final AnaliseService analiseService;

    @ExceptionHandler(ErroDominioNaoEncontrado.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleErroDominioNaoEncontrado(ErroDominioNaoEncontrado ex) {
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
                                                 @RequestBody(required = false) Map<String, String> payload) {
        if (payload == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser nulo.");
        }
        String observacoes = payload.getOrDefault("observacoes", "");
        return analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
            .subprocessoCodigo(id)
            .observacoes(observacoes)
            .tipo(TipoAnalise.CADASTRO)
            .acao(null)
            .unidadeSigla(payload.get("unidadeSigla"))
            .analistaUsuarioTitulo(payload.get("analistaUsuarioTitulo"))
            .motivo(payload.get("motivo"))
            .build());
    }

    @GetMapping("/analises-validacao")
    public List<Analise> listarAnalisesValidacao(@PathVariable("id") Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO);
    }

    @PostMapping("/analises-validacao")
    @ResponseStatus(HttpStatus.CREATED)
    public Analise criarAnaliseValidacao(@PathVariable("id") Long id,
                                                  @RequestBody(required = false) Map<String, String> payload) {
        if (payload == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser nulo.");
        }
        String observacoes = payload.getOrDefault("observacoes", "");
        return analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
            .subprocessoCodigo(id)
            .observacoes(observacoes)
            .tipo(TipoAnalise.VALIDACAO)
            .acao(null)
            .unidadeSigla(payload.get("unidadeSigla"))
            .analistaUsuarioTitulo(payload.get("analistaUsuarioTitulo"))
            .motivo(payload.get("motivo"))
            .build());
    }
}
