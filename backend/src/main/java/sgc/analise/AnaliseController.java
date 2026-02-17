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

@RestController
@RequestMapping("/api/subprocessos/{codSubprocesso}")
@RequiredArgsConstructor
@Tag(name = "Análises", description = "Endpoints para gerenciar as análises de cadastro e validação de subprocessos")
public class AnaliseController {
    private final AnaliseFacade analiseFacade;
    private final SubprocessoFacade subprocessoFacade;

    @GetMapping("/analises-cadastro")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista o histórico de análises de cadastro")
    public List<AnaliseHistoricoDto> listarAnalisesCadastro(@PathVariable("codSubprocesso") Long codigo) {
        subprocessoFacade.buscarSubprocesso(codigo); // Valida existência (lança 404 se não existir)
        return analiseFacade.listarHistoricoCadastro(codigo);
    }

    @PostMapping("/analises-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de cadastro")
    public AnaliseHistoricoDto criarAnaliseCadastro(@PathVariable Long codSubprocesso,
                                        @RequestBody @Valid CriarAnaliseRequest request) {

        return criarAnalise(codSubprocesso, request, TipoAnalise.CADASTRO);
    }

    @GetMapping("/analises-validacao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista o histórico de análises de validação")
    public List<AnaliseHistoricoDto> listarAnalisesValidacao(@PathVariable Long codSubprocesso) {
        subprocessoFacade.buscarSubprocesso(codSubprocesso);
        return analiseFacade.listarHistoricoValidacao(codSubprocesso).stream()
                .map(v -> new AnaliseHistoricoDto(v.dataHora(), v.observacoes(), v.acao(), v.unidadeSigla(), null, v.analistaUsuarioTitulo(), v.motivo(), v.tipo()))
                .toList();
    }

    @PostMapping("/analises-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long codSubprocesso, @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(codSubprocesso, request, TipoAnalise.VALIDACAO);
    }

    private AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocesso(codSubprocesso);
        String observacoes = StringUtils.stripToEmpty(request.observacoes());

        CriarAnaliseCommand command = CriarAnaliseCommand.builder()
                .codSubprocesso(codSubprocesso)
                .observacoes(observacoes)
                .tipo(tipo)
                .acao(null)
                .siglaUnidade(request.siglaUnidade())
                .tituloUsuario(request.tituloUsuario())
                .motivo(request.motivo())
                .build();

        Analise analise = analiseFacade.criarAnalise(subprocesso, command);
        return analiseFacade.paraHistoricoDto(analise);
    }
}
