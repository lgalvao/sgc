package sgc.analise;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sgc.analise.dto.CriarAnaliseRequestDto;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.TipoAnalise;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos/{id}")
@RequiredArgsConstructor
@Tag(name = "Análises", description = "Endpoints para gerenciar as análises de cadastro e validação de subprocessos")
public class AnaliseControle {
    private final AnaliseService analiseService;


    /**
     * Recupera o histórico de análises associadas à fase de cadastro de um subprocesso.
     *
     * @param id O ID do subprocesso.
     * @return Uma lista de {@link Analise} contendo o histórico de análises de cadastro.
     */
    @GetMapping("/analises-cadastro")
    @Operation(summary = "Lista o histórico de análises de cadastro")
    public List<Analise> listarAnalisesCadastro(@PathVariable("id") Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.CADASTRO);
    }

    /**
     * Registra uma nova análise para a fase de cadastro de um subprocesso.
     * <p>
     * Este endpoint recebe os dados da análise a partir de um payload flexível.
     * A análise é associada ao subprocesso identificado pelo ID na URL.
     *
     * @param id      O ID do subprocesso ao qual a análise pertence.
     * @param payload Um mapa contendo os dados da análise. Campos esperados incluem
     *                'observacoes', 'unidadeSigla', 'analistaUsuarioTitulo' e 'motivo'.
     *                O corpo da requisição não pode ser nulo.
     * @return A entidade {@link Analise} recém-criada.
     */
    @PostMapping("/analises-cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova análise de cadastro")
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

    /**
     * Recupera o histórico de análises associadas à fase de validação de um subprocesso.
     *
     * @param id O ID do subprocesso.
     * @return Uma lista de {@link Analise} contendo o histórico de análises de validação.
     */
    @GetMapping("/analises-validacao")
    @Operation(summary = "Lista o histórico de análises de validação")
    public List<Analise> listarAnalisesValidacao(@PathVariable("id") Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO);
    }

    /**
     * Registra uma nova análise para a fase de validação de um subprocesso.
     * <p>
     * Assim como na análise de cadastro, este endpoint recebe os dados da análise
     * a partir de um payload flexível e a associa ao subprocesso correspondente.
     *
     * @param id      O ID do subprocesso ao qual a análise pertence.
     * @param payload Um mapa contendo os dados da análise. Campos esperados incluem
     *                'observacoes', 'unidadeSigla', 'analistaUsuarioTitulo' e 'motivo'.
     *                O corpo da requisição não pode ser nulo.
     * @return A entidade {@link Analise} recém-criada.
     */
    @PostMapping("/analises-validacao")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova análise de validação")
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
