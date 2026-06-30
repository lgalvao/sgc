package sgc.subprocesso;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.ComumDtos.*;
import sgc.mapa.dto.*;
import sgc.organizacao.*;
import sgc.organizacao.service.*;
import sgc.seguranca.*;
import sgc.seguranca.sanitizacao.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.net.*;
import java.util.*;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints unificados para gerenciamento de subprocessos, cadastro, mapa e validação")
@PreAuthorize("isAuthenticated()")
public class SubprocessoController {

    private final SubprocessoConsultaService consultaService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoTransicaoService transicaoService;
    private final UnidadeService unidadeService;
    private final SubprocessoDtoMapper subprocessoDtoMapper;
    private final SubprocessoApresentacaoService subprocessoApresentacaoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SubprocessoListagemDto> listar() {
        return consultaService.listarTodos().stream()
                .map(subprocessoDtoMapper::paraListagem)
                .toList();
    }

    @GetMapping("/{codSubprocesso}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public SubprocessoDetalheResponse obterPorCodigo(@PathVariable Long codSubprocesso) {
        return consultaService.obterDetalhes(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/status")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém apenas o status atual do subprocesso")
    public ResponseEntity<SubprocessoSituacaoDto> obterStatus(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterStatus(codSubprocesso));
    }

    @GetMapping("/buscar")
    @PostAuthorize("hasPermission(returnObject.body.codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<SubprocessoCodigoDto> buscarPorProcessoEUnidade(
            @RequestParam Long codProcesso, @RequestParam String siglaUnidade) {
        Long codUnidade = unidadeService.buscarCodigoPorSigla(siglaUnidade);
        Subprocesso sp = consultaService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
        return ResponseEntity.ok(new SubprocessoCodigoDto(sp.getCodigo()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubprocessoResumoDto> criar(@Valid @RequestBody CriarSubprocessoRequest request) {
        var salvo = subprocessoService.criarEntidade(request);
        Long codSubprocesso = salvo.getCodigo();
        var subprocessoCriado = consultaService.buscarSubprocesso(codSubprocesso);
        URI uri = URI.create("/api/subprocessos/%d".formatted(codSubprocesso));
        return ResponseEntity.created(uri).body(subprocessoDtoMapper.paraResumo(subprocessoCriado));
    }

    @PostMapping("/{codSubprocesso}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubprocessoResumoDto> atualizar(
            @PathVariable Long codSubprocesso, @Valid @RequestBody AtualizarSubprocessoRequest request) {
        subprocessoService.atualizarEntidade(codSubprocesso, request.paraCommand());
        var subprocessoAtualizado = consultaService.buscarSubprocesso(codSubprocesso);
        return ResponseEntity.ok(subprocessoDtoMapper.paraResumo(subprocessoAtualizado));
    }

    @PostMapping("/{codSubprocesso}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codSubprocesso) {
        subprocessoService.excluir(codSubprocesso);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codSubprocesso}/data-limite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarDataLimite(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid DataRequest request) {
        transicaoService.alterarDataLimite(codSubprocesso, request.data());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/contexto-edicao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicao(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterContextoEdicao(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/permissoes-ui")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<PermissoesSubprocessoDto> obterPermissoesUI(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterPermissoesUI(codSubprocesso));
    }

    @GetMapping("/contexto-edicao/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicaoPorProcessoEUnidade(
            @RequestParam Long codProcesso,
            @RequestParam String siglaUnidade
    ) {
        return ResponseEntity.ok(subprocessoApresentacaoService.obterContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade));
    }

    @PostMapping("/{codSubprocesso}/importar-atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'IMPORTAR_ATIVIDADES')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public AtividadeOperacaoResponse importarAtividades(
            @PathVariable Long codSubprocesso, @RequestBody @Valid ImportarAtividadesRequest request) {
        return subprocessoApresentacaoService.importarAtividades(codSubprocesso, request);
    }

    @GetMapping("/{codSubprocesso}/impactos-mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VERIFICAR_IMPACTOS')")
    public ImpactoMapaResponse verificarImpactos(@PathVariable Long codSubprocesso) {
        return consultaService.verificarImpactos(codSubprocesso);
    }

    @PostMapping("/{codSubprocesso}/disponibilizar-mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza o mapa para validação")
    public ResponseEntity<MensagemResponse> disponibilizarMapa(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody DisponibilizarMapaRequest request) {
        transicaoService.disponibilizarMapa(codSubprocesso, request);
        return ResponseEntity.ok(new MensagemResponse("Mapa de competências disponibilizado."));
    }

    @GetMapping("/{codSubprocesso}/mapa-visualizacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa formatado para visualização")
    public MapaVisualizacaoResponse obterMapaParaVisualizacao(@PathVariable Long codSubprocesso) {
        return consultaService.mapaParaVisualizacao(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/mapa-completo")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa completo para edição/visualização")
    @Transactional(readOnly = true)
    public ResponseEntity<MapaCompletoDto> obterMapaCompleto(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso));
    }

    @PostMapping("/{codSubprocesso}/mapa-completo")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva o mapa completo (batch)")
    public ResponseEntity<MapaCompletoDto> salvarMapaCompleto(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody SalvarMapaRequest request) {

        subprocessoService.salvarMapa(codSubprocesso, request);
        return ResponseEntity.ok(consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso));
    }

    @PostMapping("/disponibilizar-mapa-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza mapas em bloco")
    public void disponibilizarMapaEmBloco(@RequestBody @Valid ProcessarEmBlocoRequest request) {
        DisponibilizarMapaRequest dispoReq;
        dispoReq = DisponibilizarMapaRequest.builder()
                .dataLimite(request.dataLimite())
                .build();
        transicaoService.disponibilizarMapaEmBloco(request.subprocessos(), dispoReq);
    }

    @GetMapping("/{codSubprocesso}/mapa-ajuste")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Obtém dados do mapa preparados para ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long codSubprocesso) {
        return consultaService.obterMapaParaAjuste(codSubprocesso);
    }

    @PostMapping("/{codSubprocesso}/mapa-ajuste/atualizar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva os ajustes feitos no mapa")
    public void salvarAjustesMapa(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid SalvarAjustesRequest request) {
        subprocessoService.salvarAjustesMapa(codSubprocesso, request.competencias());
    }

    @PostMapping("/{codSubprocesso}/competencia")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Adiciona uma competência ao mapa")
    public ResponseEntity<MapaCompletoDto> adicionarCompetencia(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody CriarCompetenciaRequest request) {
        subprocessoService.adicionarCompetencia(codSubprocesso, request);
        return ResponseEntity.ok(consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso));
    }

    @PostMapping("/{codSubprocesso}/competencia/{codCompetencia}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Atualiza uma competência do mapa")
    public ResponseEntity<MapaCompletoDto> atualizarCompetencia(
            @PathVariable Long codSubprocesso,
            @PathVariable Long codCompetencia,
            @Valid @RequestBody AtualizarCompetenciaRequest request) {
        subprocessoService.atualizarCompetencia(codSubprocesso, codCompetencia, request);
        return ResponseEntity.ok(consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso));
    }

    @PostMapping("/{codSubprocesso}/competencia/{codCompetencia}/remover")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Remove uma competência do mapa")
    public ResponseEntity<MapaCompletoDto> removerCompetencia(
            @PathVariable Long codSubprocesso,
            @PathVariable Long codCompetencia) {
        subprocessoService.removerCompetencia(codSubprocesso, codCompetencia);
        return ResponseEntity.ok(consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso));
    }

    @PostMapping("/{codSubprocesso}/apresentar-sugestoes")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'APRESENTAR_SUGESTOES')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid TextoOpcionalRequest request) {
        transicaoService.apresentarSugestoes(codSubprocesso, request.texto());
    }

    @GetMapping("/{codSubprocesso}/sugestoes")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public SugestoesDto obterSugestoes(@PathVariable Long codSubprocesso) {
        return consultaService.obterSugestoes(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/historico-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoValidacao(@PathVariable Long codSubprocesso) {
        return consultaService.listarHistoricoValidacao(codSubprocesso);
    }

    @PostMapping("/{codSubprocesso}/validar-mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VALIDAR_MAPA')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public ResponseEntity<Void> validarMapa(@PathVariable Long codSubprocesso) {
        transicaoService.validarMapa(codSubprocesso);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/devolver-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_MAPA')")
    @Operation(summary = "Devolve o mapa para ajuste (pelo chefe/gestor)")
    public ResponseEntity<Void> devolverValidacao(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid JustificativaRequest request) {
        transicaoService.devolverValidacao(codSubprocesso, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/aceitar-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita a validação (pelo gestor)")
    public ResponseEntity<Void> aceitarValidacao(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) TextoOpcionalRequest request) {
        String observacoes = sanitizarTextoOpcional(request);
        transicaoService.aceitarValidacao(codSubprocesso, observacoes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/homologar-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa a validação")
    public ResponseEntity<Void> homologarValidacao(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) TextoOpcionalRequest request) {
        String observacoes = sanitizarTextoOpcional(request);
        transicaoService.homologarValidacao(codSubprocesso, observacoes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/submeter-mapa-ajustado")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Submete o mapa após ajustes solicitados")
    public ResponseEntity<Void> submeterMapaAjustado(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody SubmeterMapaAjustadoRequest request) {
        transicaoService.submeterMapaAjustado(codSubprocesso, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/aceitar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@RequestBody @Valid ProcessarEmBlocoRequest request) {
        transicaoService.aceitarValidacaoEmBloco(request.subprocessos());
    }

    @PostMapping("/homologar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@RequestBody @Valid ProcessarEmBlocoRequest request) {
        transicaoService.homologarValidacaoEmBloco(request.subprocessos());
    }

    @PostMapping("/{codSubprocesso}/analises-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_CADASTRO')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de cadastro")
    public AnaliseHistoricoDto criarAnaliseCadastro(@PathVariable Long codSubprocesso,
                                                    @RequestBody @Valid CriarAnaliseRequest request) {
        return subprocessoApresentacaoService.criarAnalise(codSubprocesso, request, TipoAnalise.CADASTRO);
    }

    @PostMapping("/{codSubprocesso}/analises-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_MAPA')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long codSubprocesso,
                                                     @RequestBody @Valid CriarAnaliseRequest request) {
        return subprocessoApresentacaoService.criarAnalise(codSubprocesso, request, TipoAnalise.VALIDACAO);
    }

    private @Nullable String sanitizarTextoOpcional(@Nullable TextoOpcionalRequest request) {
        if (request == null || request.texto() == null) {
            return null;
        }
        return UtilSanitizacao.sanitizarFormatado(request.texto());
    }

}
