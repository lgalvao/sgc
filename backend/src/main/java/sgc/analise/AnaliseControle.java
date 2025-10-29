package sgc.analise;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sgc.analise.dto.CriarAnaliseRequestDto;
import sgc.comum.erros.ErroNegocio;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.TipoAnalise;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gerenciar as análises de subprocessos.
 * <p>
 * Fornece endpoints para criar e listar análises relacionadas às fases de
 * cadastro e validação de um subprocesso.
 */
@RestController
@RequestMapping("/api/subprocessos/{codigo}")
@RequiredArgsConstructor
@Tag(name = "Análises", description = "Endpoints para gerenciar as análises de cadastro e validação de subprocessos")
public class AnaliseControle {
    private final AnaliseService analiseService;

    /**
     * Recupera o histórico de análises associadas à fase de cadastro de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Uma lista de {@link Analise} contendo o histórico de análises de cadastro.
     */
    @GetMapping("/analises-cadastro")
    @Operation(summary = "Lista o histórico de análises de cadastro")
    public List<Analise> listarAnalisesCadastro(@PathVariable("codigo") Long codigo) {
        return analiseService.listarPorSubprocesso(codigo, TipoAnalise.CADASTRO);
    }

    /**
     * Registra uma nova análise para a fase de cadastro de um subprocesso.
     * <p>
     * Este endpoint recebe os dados da análise a partir de um corpo flexível.
     * A análise é associada ao subprocesso identificado pelo código na URL.
     *
     * @param codigo      O código do subprocesso ao qual a análise pertence.
     * @param corpo Um mapa contendo os dados da análise. Campos esperados incluem
     *                'observacoes', 'unidadeSigla', 'analistaUsuarioTitulo' e 'motivo'.
     *                O corpo da requisição não pode ser nulo.
     * @return A entidade {@link Analise} recém-criada.
     */
    @PostMapping("/analises-cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova análise de cadastro")
    public Analise criarAnaliseCadastro(@PathVariable("codigo") Long codigo,
                                        @RequestBody(required = false) Map<String, String> corpo) {

        if (corpo == null) {
            throw new ErroNegocio("O corpo da requisição não pode ser nulo.");
        }

        String observacoes = corpo.getOrDefault("observacoes", "");
        return analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codigo)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(corpo.get("unidadeSigla"))
                .analistaUsuarioTitulo(corpo.get("analistaUsuarioTitulo"))
                .motivo(corpo.get("motivo"))
                .build());
    }

    /**
     * Recupera o histórico de análises associadas à fase de validação de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Uma lista de {@link Analise} contendo o histórico de análises de validação.
     */
    @GetMapping("/analises-validacao")
    @Operation(summary = "Lista o histórico de análises de validação")
    public List<Analise> listarAnalisesValidacao(@PathVariable("codigo") Long codigo) {
        return analiseService.listarPorSubprocesso(codigo, TipoAnalise.VALIDACAO);
    }

    /**
     * Registra uma nova análise para a fase de validação de um subprocesso.
     * <p>
     * Assim como na análise de cadastro, este endpoint recebe os dados da análise
     * a partir de um corpo flexível e a associa ao subprocesso correspondente.
     *
     * @param codigo      O código do subprocesso ao qual a análise pertence.
     * @param corpo Um mapa contendo os dados da análise. Campos esperados incluem
     *                'observacoes', 'unidadeSigla', 'analistaUsuarioTitulo' e 'motivo'.
     *                O corpo da requisição não pode ser nulo.
     * @return A entidade {@link Analise} recém-criada.
     */
    @PostMapping("/analises-validacao")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova análise de validação")
    public Analise criarAnaliseValidacao(@PathVariable("codigo") Long codigo,
                                         @RequestBody(required = false) Map<String, String> corpo) {
        if (corpo == null) {
            throw new ErroNegocio("O corpo da requisição não pode ser nulo.");
        }

        String observacoes = corpo.getOrDefault("observacoes", "");
        return analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codigo)
                .observacoes(observacoes)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(corpo.get("unidadeSigla"))
                .analistaUsuarioTitulo(corpo.get("analistaUsuarioTitulo"))
                .motivo(corpo.get("motivo"))
                .build());
    }
}
