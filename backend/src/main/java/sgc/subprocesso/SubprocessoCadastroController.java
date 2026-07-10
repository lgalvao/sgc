package sgc.subprocesso;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.ComumDtos.*;
import sgc.mapa.dto.*;
import sgc.seguranca.sanitizacao.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.*;

import java.util.*;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos - Cadastro", description = "Endpoints do fluxo de cadastro e revisão de cadastro")
@PreAuthorize("isAuthenticated()")
public class SubprocessoCadastroController {

    private final SubprocessoConsultaService consultaService;
    private final CadastroFluxoService cadastroFluxoService;
    private final SubprocessoApresentacaoService subprocessoApresentacaoService;

    @PostMapping("/{codSubprocesso}/reabrir-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre o cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirCadastro(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid JustificativaRequest request) {
        cadastroFluxoService.reabrirCadastro(codSubprocesso, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/reabrir-revisao-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre a revisão de cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid JustificativaRequest request) {
        cadastroFluxoService.reabrirRevisaoCadastro(codSubprocesso, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/historico-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long codSubprocesso) {
        return consultaService.listarHistoricoCadastro(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/contexto-cadastro-atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoCadastroAtividadesResponse> obterContextoCadastroAtividades(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterContextoCadastroAtividades(codSubprocesso));
    }

    @GetMapping("/contexto-cadastro-atividades/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContextoCadastroAtividadesResponse> obterContextoCadastroAtividadesPorProcessoEUnidade(
            @RequestParam Long codProcesso,
            @RequestParam String siglaUnidade
    ) {
        return ResponseEntity.ok(subprocessoApresentacaoService.obterContextoCadastroAtividadesPorProcessoEUnidade(codProcesso, siglaUnidade));
    }

    @GetMapping("/{codSubprocesso}/atividades-importacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CONSULTAR_PARA_IMPORTACAO')")
    @Operation(summary = "Lista todas as atividades de um subprocesso finalizado para importação")
    public ResponseEntity<List<AtividadeDto>> listarAtividadesParaImportacao(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.listarAtividadesParaImportacao(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/validar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Valida se o cadastro está pronto para disponibilização")
    public ResponseEntity<ValidacaoCadastroDto> validarCadastro(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.validarCadastro(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public SubprocessoCadastroDto obterCadastro(@PathVariable Long codSubprocesso) {
        return consultaService.obterCadastro(codSubprocesso);
    }

    @PostMapping("/{codSubprocesso}/cadastro/disponibilizar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_CADASTRO')")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(@PathVariable Long codSubprocesso) {
        cadastroFluxoService.disponibilizarCadastro(codSubprocesso);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{codSubprocesso}/iniciar-revisao-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_CADASTRO')")
    @Operation(summary = "Inicia a revisão do cadastro de atividades (transiciona de NAO_INICIADO para REVISAO_CADASTRO_EM_ANDAMENTO)")
    public ResponseEntity<MensagemResponse> iniciarRevisaoCadastro(@PathVariable Long codSubprocesso) {
        cadastroFluxoService.iniciarRevisaoCadastro(codSubprocesso);
        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro iniciada"));
    }

    @PostMapping("/{codSubprocesso}/cancelar-inicio-revisao-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_CADASTRO')")
    @Operation(summary = "Cancela o início da revisão do cadastro de atividades (transiciona de REVISAO_CADASTRO_EM_ANDAMENTO para NAO_INICIADO)")
    public ResponseEntity<MensagemResponse> cancelarInicioRevisaoCadastro(@PathVariable Long codSubprocesso) {
        cadastroFluxoService.cancelarInicioRevisaoCadastro(codSubprocesso);
        return ResponseEntity.ok(new MensagemResponse("Início da revisão do cadastro cancelado"));
    }

    @PostMapping("/{codSubprocesso}/disponibilizar-revisao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_REVISAO_CADASTRO')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(@PathVariable Long codSubprocesso) {
        cadastroFluxoService.disponibilizarRevisao(codSubprocesso);
        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro disponibilizada"));
    }

    @PostMapping("/{codSubprocesso}/devolver-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_CADASTRO')")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(@PathVariable Long codSubprocesso, @Valid @RequestBody JustificativaRequest request) {
        cadastroFluxoService.devolver(codSubprocesso, sanitizar(request.justificativa()));
    }

    @PostMapping("/{codSubprocesso}/aceitar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(@PathVariable Long codSubprocesso, @Valid @RequestBody TextoOpcionalRequest request) {
        cadastroFluxoService.aceitar(codSubprocesso, sanitizar(request.texto()));
    }

    @PostMapping("/{codSubprocesso}/homologar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(@PathVariable Long codSubprocesso, @Valid @RequestBody TextoOpcionalRequest request) {
        cadastroFluxoService.homologar(codSubprocesso, sanitizar(request.texto()));
    }

    @PostMapping("/{codSubprocesso}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_REVISAO_CADASTRO')")
    public void devolverRevisaoCadastro(@PathVariable Long codSubprocesso, @Valid @RequestBody JustificativaRequest request) {
        cadastroFluxoService.devolver(codSubprocesso, sanitizar(request.justificativa()));
    }

    @PostMapping("/{codSubprocesso}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_REVISAO_CADASTRO')")
    public void aceitarRevisaoCadastro(@PathVariable Long codSubprocesso, @Valid @RequestBody TextoOpcionalRequest request) {
        cadastroFluxoService.aceitar(codSubprocesso, sanitizar(request.texto()));
    }

    @PostMapping("/{codSubprocesso}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_REVISAO_CADASTRO')")
    public void homologarRevisaoCadastro(@PathVariable Long codSubprocesso, @Valid @RequestBody TextoOpcionalRequest request) {
        cadastroFluxoService.homologar(codSubprocesso, sanitizar(request.texto()));
    }

    @PostMapping("/aceitar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@RequestBody @Valid ProcessarEmBlocoRequest request) {
        cadastroFluxoService.aceitarCadastroEmBloco(request.subprocessos());
    }

    @PostMapping("/homologar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@RequestBody @Valid ProcessarEmBlocoRequest request) {
        cadastroFluxoService.homologarCadastroEmBloco(request.subprocessos());
    }

    private String sanitizar(@Nullable String texto) {
        return Optional.ofNullable(texto)
                .map(UtilSanitizacao::sanitizar)
                .orElse("");
    }
}
