package sgc.analise;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroNegocio;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gerenciar as análises de subprocessos.
 * <p>
 * Fornece endpoints para criar e listar análises relacionadas às fases de
 * cadastro e validação de um subprocesso.
 */
@RestController
@RequestMapping("/api/subprocessos/{codSubprocesso}")
@RequiredArgsConstructor
@Tag(name = "Análises", description = "Endpoints para gerenciar as análises de cadastro e validação de subprocessos")
public class AnaliseController {
    private final AnaliseService analiseService;

    /**
     * Recupera o histórico de análises associadas à fase de cadastro de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Uma lista de {@link Analise} contendo o histórico de análises de cadastro.
     */
    @GetMapping("/analises-cadastro")
    @Operation(summary = "Lista o histórico de análises de cadastro")
    public List<Analise> listarAnalisesCadastro(@PathVariable("codSubprocesso") Long codigo) {
        return analiseService.listarPorSubprocesso(codigo, TipoAnalise.CADASTRO);
    }

    /**
     * Registra uma nova análise para a fase de cadastro de um subprocesso.
     * <p>
     * Este endpoint recebe os dados da análise a partir de um corpo flexível.
     * A análise é associada ao subprocesso identificado pelo código na URL.
     *
     * @param codSubprocesso O código do subprocesso ao qual a análise pertence.
     * @param corpo  Um mapa contendo os dados da análise. Campos esperados incluem
     *               'observacoes', 'siglaUnidade', 'tituloUsuario' e 'motivo'.
     *               O corpo da requisição não pode ser nulo.
     * @return A entidade {@link Analise} recém-criada.
     */
    @PostMapping("/analises-cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova análise de cadastro")
    public Analise criarAnaliseCadastro(@PathVariable("codSubprocesso") Long codSubprocesso,
                                        @RequestBody(required = false) Map<String, String> corpo) {

        // TODO este tratamento está muito geral. E nem me parece bem um erro de negócio
        if (corpo == null) throw new ErroNegocio("O corpo da requisição não pode ser nulo.");

        String observacoes = corpo.getOrDefault("observacoes", "");

        // TODO este código repete quase igual no método 'criarAnaliseValidacao'
        CriarAnaliseRequest criarAnaliseRequest = CriarAnaliseRequest.builder()
                .codSubprocesso(codSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .siglaUnidade(corpo.get("siglaUnidade"))
                .tituloUsuario(corpo.get("tituloUsuario"))
                .motivo(corpo.get("motivo"))
                .build();

        return analiseService.criarAnalise(criarAnaliseRequest);
    }

    /**
     * Recupera o histórico de análises associadas à fase de validação de um subprocesso.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Uma lista de {@link Analise} contendo o histórico de análises de validação.
     */
    @GetMapping("/analises-validacao")
    @Operation(summary = "Lista o histórico de análises de validação")
    public List<Analise> listarAnalisesValidacao(@PathVariable("codSubprocesso") Long codSubprocesso) {
        return analiseService.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO);
    }

    /**
     * Registra uma nova análise para a fase de validação de um subprocesso.
     * <p>
     * Assim como na análise de cadastro, este endpoint recebe os dados da análise
     * a partir de um corpo flexível e a associa ao subprocesso correspondente.
     *
     * @param codSubprocesso O código do subprocesso ao qual a análise pertence.
     * @param corpo  Um mapa contendo os dados da análise. Campos esperados incluem
     *               'observacoes', 'siglaUnidade', 'tituloUsuario' e 'motivo'.
     *               O corpo da requisição não pode ser nulo.
     * @return A entidade {@link Analise} recém-criada.
     */
    @PostMapping("/analises-validacao")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova análise de validação")
    public Analise criarAnaliseValidacao(@PathVariable("codSubprocesso") Long codSubprocesso,
                                         @RequestBody(required = false) Map<String, String> corpo) {

        // TODO este tratamento está muito geral. E nem me parece bem um erro de negócio
        if (corpo == null) throw new ErroNegocio("O corpo da requisição não pode ser nulo.");

        String observacoes = corpo.getOrDefault("observacoes", "");
        CriarAnaliseRequest criarAnaliseRequest = CriarAnaliseRequest.builder()
                .codSubprocesso(codSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .siglaUnidade(corpo.get("siglaUnidade"))
                .tituloUsuario(corpo.get("tituloUsuario"))
                .motivo(corpo.get("motivo"))
                .build();

        return analiseService.criarAnalise(criarAnaliseRequest);
    }
}
