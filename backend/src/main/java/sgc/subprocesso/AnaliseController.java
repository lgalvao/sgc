package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.dto.CriarAnaliseCommand;
import sgc.subprocesso.dto.CriarAnaliseRequest;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;

@RestController
@RequestMapping("/api/subprocessos/{codSubprocesso}")
@RequiredArgsConstructor
@Tag(name = "Análises", description = "Gerenciamento de análises de cadastro e validação")
public class AnaliseController {
    private final AnaliseFacade analiseFacade;
    private final SubprocessoFacade subprocessoFacade;

    @GetMapping("/analises-cadastro")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista o histórico de análises de cadastro")
    public List<AnaliseHistoricoDto> listarAnalisesCadastro(@PathVariable("codSubprocesso") Long codigo) {
        subprocessoFacade.buscarSubprocesso(codigo);
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
        return analiseFacade.listarHistoricoValidacao(codSubprocesso);
    }

    @PostMapping("/analises-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long codSubprocesso,
                                                     @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(codSubprocesso, request, TipoAnalise.VALIDACAO);
    }

    // TODO essa logica deveria estar no facade o service! Inclusive ja tem um quase igual la.
    private AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo) {
        Subprocesso sp = subprocessoFacade.buscarSubprocesso(codSubprocesso);
        String obs = StringUtils.stripToEmpty(request.observacoes());

        CriarAnaliseCommand criarAnaliseCommand = CriarAnaliseCommand.builder()
                .codSubprocesso(codSubprocesso)
                .observacoes(obs)
                .tipo(tipo)
                .acao(request.acao())
                .siglaUnidade(request.siglaUnidade())
                .tituloUsuario(request.tituloUsuario())
                .motivo(request.motivo())
                .build();

        Analise analise = analiseFacade.criarAnalise(sp, criarAnaliseCommand);
        return analiseFacade.paraHistoricoDto(analise);
    }
}
