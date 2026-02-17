package sgc.analise;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;

/**
 * Controlador REST para gerenciar as análises de subprocessos.
 *
 * <p>Fornece endpoints para criar e listar análises relacionadas às fases de cadastro e validação
 * de um subprocesso.
 */
@RestController
@RequestMapping("/api/subprocessos/{codSubprocesso}")
@RequiredArgsConstructor
@Tag(name = "Análises", description = "Endpoints para gerenciar as análises de cadastro e validação de subprocessos")
public class AnaliseController {
    private final AnaliseFacade analiseFacade;
    private final SubprocessoFacade subprocessoFacade;

    /**
     * Recupera o histórico de análises associadas à fase de cadastro de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Uma lista de {@link AnaliseHistoricoDto} contendo o histórico de análises de cadastro.
     */
    @GetMapping("/analises-cadastro")
    // TODO protecao esta errada aqui. Apenas os perfis ADMIN, GESTOR e CHEFE podem ver
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista o histórico de análises de cadastro")
    public List<AnaliseHistoricoDto> listarAnalisesCadastro(@PathVariable("codSubprocesso") Long codigo) {
        subprocessoFacade.buscarSubprocesso(codigo);
        return analiseFacade.listarHistoricoCadastro(codigo);
    }

    /**
     * Registra uma nova análise para a fase de cadastro de um subprocesso.
     *
     * <p>Este endpoint recebe os dados da análise a partir de um corpo estruturado. A análise é
     * associada ao subprocesso identificado pelo código na URL.
     *
     * @param codSubprocesso O código do subprocesso ao qual a análise pertence.
     * @param request        O DTO contendo os dados da análise. Campos esperados incluem 'observacoes',
     *                       'siglaUnidade', 'tituloUsuario' e 'motivo'.
     * @return O DTO {@link AnaliseHistoricoDto} recém-criado.
     */
    @PostMapping("/analises-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de cadastro")
    public AnaliseHistoricoDto criarAnaliseCadastro(@PathVariable Long codSubprocesso,
                                                    @RequestBody @Valid CriarAnaliseRequest request) {

        return criarAnalise(codSubprocesso, request, TipoAnalise.CADASTRO);
    }

    /**
     * Recupera o histórico de análises associadas à fase de validação de um subprocesso.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Uma lista de {@link AnaliseHistoricoDto} contendo o histórico de análises de validação.
     */
    @GetMapping("/analises-validacao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista o histórico de análises de validação")
    public List<AnaliseHistoricoDto> listarAnalisesValidacao(@PathVariable Long codSubprocesso) {
        subprocessoFacade.buscarSubprocesso(codSubprocesso);
        return analiseFacade.listarHistoricoValidacao(codSubprocesso).stream()
                .map(v -> {
                    // TODO usar Builder para construir o dto.
                    AnaliseHistoricoDto analiseHistoricoDto = new AnaliseHistoricoDto(
                            v.dataHora(),
                            v.observacoes(),
                            v.acao(),
                            v.unidadeSigla(),
                            null,
                            v.analistaUsuarioTitulo(),
                            v.motivo(),
                            v.tipo());
                    return analiseHistoricoDto;
                })
                .toList();
    }

    /**
     * Registra uma nova análise para a fase de validação de um subprocesso.
     *
     * <p>Assim como na análise de cadastro, este endpoint recebe os dados da análise a partir de um
     * corpo estruturado e a associa ao subprocesso correspondente.
     *
     * @param codSubprocesso O código do subprocesso ao qual a análise pertence.
     * @param request        O DTO contendo os dados da análise. Campos esperados incluem 'observacoes',
     *                       'siglaUnidade', 'tituloUsuario' e 'motivo'.
     * @return O DTO {@link AnaliseHistoricoDto} recém-criado.
     */
    @PostMapping("/analises-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long codSubprocesso,
                                                     @RequestBody @Valid CriarAnaliseRequest request) {

        return criarAnalise(codSubprocesso, request, TipoAnalise.VALIDACAO);
    }

    private AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo) {
        Subprocesso sp = subprocessoFacade.buscarSubprocesso(codSubprocesso);
        String obs = StringUtils.stripToEmpty(request.observacoes());

        // TODO estranho essa acao nula!
        CriarAnaliseCommand criarAnaliseCommand = CriarAnaliseCommand.builder()
                .codSubprocesso(codSubprocesso)
                .observacoes(obs)
                .tipo(tipo)
                .acao(null)
                .siglaUnidade(request.siglaUnidade())
                .tituloUsuario(request.tituloUsuario())
                .motivo(request.motivo())
                .build();

        Analise analise = analiseFacade.criarAnalise(sp, criarAnaliseCommand);
        return analiseFacade.paraHistoricoDto(analise);
    }
}
