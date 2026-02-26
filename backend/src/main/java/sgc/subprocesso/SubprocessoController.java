package sgc.subprocesso;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.ComumDtos.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
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
@Slf4j
public class SubprocessoController {

    private final SubprocessoService subprocessoService;
    private final OrganizacaoFacade organizacaoFacade; // Mantido para suporte a busca por sigla

    // ============================================================================================
    // LEITURA / CRUD (Migrado de SubprocessoCrudController)
    // ============================================================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public List<Subprocesso> listar() {
        return subprocessoService.listarEntidades();
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public SubprocessoDetalheResponse obterPorCodigo(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.obterDetalhes(codigo, usuario);
    }

    @GetMapping("/{codigo}/status")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém apenas o status atual do subprocesso")
    public ResponseEntity<SubprocessoSituacaoDto> obterStatus(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoService.obterStatus(codigo));
    }

    @GetMapping("/buscar")
    @PostAuthorize("hasPermission(returnObject.body, 'VISUALIZAR_SUBPROCESSO')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> buscarPorProcessoEUnidade(
            @RequestParam Long codProcesso, @RequestParam String siglaUnidade) {
        UnidadeDto unidade = organizacaoFacade.buscarPorSigla(siglaUnidade);
        Subprocesso sp = subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, unidade.getCodigo());
        return ResponseEntity.ok(sp);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> criar(@Valid @RequestBody CriarSubprocessoRequest request) {
        var salvo = subprocessoService.criarEntidade(request);
        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody AtualizarSubprocessoRequest request) {
        var atualizado = subprocessoService.atualizarEntidade(codigo, request);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{codigo}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        subprocessoService.excluir(codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/data-limite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarDataLimite(
            @PathVariable Long codigo,
            @RequestBody @Valid DataRequest request) {
        subprocessoService.alterarDataLimite(codigo, request.data());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/reabrir-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre o cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirCadastro(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request) {
        subprocessoService.reabrirCadastro(codigo, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/reabrir-revisao-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre a revisão de cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirRevisaoCadastro(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request) {
        subprocessoService.reabrirRevisaoCadastro(codigo, request.justificativa());
        return ResponseEntity.ok().build();
    }

    // ============================================================================================
    // CADASTRO / ATIVIDADES (Migrado de SubprocessoCadastroController)
    // ============================================================================================

    @GetMapping("/{codigo}/historico-cadastro")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long codigo) {
        return subprocessoService.listarHistoricoCadastro(codigo);
    }

    @GetMapping("/{codigo}/contexto-edicao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicao(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoService.obterContextoEdicao(codigo));
    }

    @GetMapping("/{codigo}/validar-cadastro")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Valida se o cadastro está pronto para disponibilização")
    public ResponseEntity<ValidacaoCadastroDto> validarCadastro(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoService.validarCadastro(codigo));
    }

    @JsonView(MapaViews.Publica.class)
    @GetMapping("/{codigo}/cadastro")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public Subprocesso obterCadastro(@PathVariable Long codigo) {
        return subprocessoService.buscarSubprocesso(codigo);
    }

    @PostMapping("/{codigo}/cadastro/disponibilizar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_CADASTRO')")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(
            @PathVariable("codigo") Long codSubprocesso,
            @AuthenticationPrincipal Usuario usuario) {

        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(codSubprocesso);
        if (!faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("codigo", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();

            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoService.disponibilizarCadastro(codSubprocesso, usuario);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{codigo}/disponibilizar-revisao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DISPONIBILIZAR_REVISAO_CADASTRO')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(
            @PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {

        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(codigo);
        if (!faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("codigo", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();

            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoService.disponibilizarRevisao(codigo, usuario);

        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro de atividades disponibilizada"));
    }

    @PostMapping("/{codigo}/devolver-cadastro")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DEVOLVER_CADASTRO')")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.justificativa()))
                .orElse("");

        subprocessoService.devolverCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/aceitar-cadastro")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.aceitarCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/homologar-cadastro")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.homologarCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DEVOLVER_REVISAO_CADASTRO')")
    public void devolverRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.justificativa()))
                .orElse("");

        subprocessoService.devolverRevisaoCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'ACEITAR_REVISAO_CADASTRO')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.aceitarRevisaoCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'HOMOLOGAR_REVISAO_CADASTRO')")
    public void homologarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.homologarRevisaoCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/importar-atividades")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'IMPORTAR_ATIVIDADES')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long codigo, @RequestBody @Valid ImportarAtividadesRequest request) {
        subprocessoService.importarAtividades(codigo, request.codSubprocessoOrigem());
        return Map.of("message", "Atividades importadas.");
    }

    @PostMapping("/{codigo}/aceitar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.aceitarCadastroEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codigo}/homologar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.homologarCadastroEmBloco(request.subprocessos(), usuario);
    }

    // ============================================================================================
    // MAPA (Migrado de SubprocessoMapaController)
    // ============================================================================================

    @GetMapping("/{codigo}/impactos-mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VERIFICAR_IMPACTOS')")
    @JsonView(MapaViews.Publica.class)
    public ImpactoMapaResponse verificarImpactos(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.verificarImpactos(codigo, usuario);
    }

    @GetMapping("/{codigo}/mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @JsonView(MapaViews.Publica.class)
    public Mapa obterMapa(@PathVariable Long codigo) {
        Subprocesso sp = subprocessoService.buscarSubprocessoComMapa(codigo);
        return sp.getMapa();
    }

    @PostMapping("/{codigo}/disponibilizar-mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza o mapa para validação")
    public ResponseEntity<MensagemResponse> disponibilizarMapa(
            @PathVariable Long codigo,
            @Valid @RequestBody DisponibilizarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.disponibilizarMapa(codigo, request, usuario);
        return ResponseEntity.ok(new MensagemResponse("Mapa de competências disponibilizado."));
    }

    @GetMapping("/{codigo}/mapa-visualizacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa formatado para visualização")
    @JsonView(MapaViews.Publica.class)
    public MapaVisualizacaoResponse obterMapaParaVisualizacao(@PathVariable Long codigo) {
        return subprocessoService.mapaParaVisualizacao(codigo);
    }

    @PostMapping("/{codigo}/mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva as alterações do mapa")
    @JsonView(MapaViews.Publica.class)
    public Mapa salvarMapa(
            @PathVariable Long codigo,
            @Valid @RequestBody SalvarMapaRequest request) {
        return subprocessoService.salvarMapa(codigo, request);
    }

    @GetMapping("/{codigo}/mapa-completo")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa completo para edição/visualização")
    @JsonView(MapaViews.Publica.class)
    @Transactional(readOnly = true)
    public ResponseEntity<Mapa> obterMapaCompleto(@PathVariable Long codigo) {
        try {
            Mapa mapa = subprocessoService.mapaCompletoPorSubprocesso(codigo);
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            log.error("Erro ao buscar mapa completo para subprocesso {}: {}", codigo, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{codigo}/mapa-completo")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva o mapa completo (batch)")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> salvarMapaCompleto(
            @PathVariable Long codigo,
            @Valid @RequestBody SalvarMapaRequest request) {

        Mapa mapa = subprocessoService.salvarMapa(codigo, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/disponibilizar-mapa-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza mapas em bloco")
    public void disponibilizarMapaEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        DisponibilizarMapaRequest dispoReq = DisponibilizarMapaRequest.builder()
                .dataLimite(request.dataLimite())
                .build();
        subprocessoService.disponibilizarMapaEmBloco(request.subprocessos(), dispoReq, usuario);
    }

    @GetMapping("/{codigo}/mapa-ajuste")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Obtém dados do mapa preparados para ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long codigo) {
        return subprocessoService.obterMapaParaAjuste(codigo);
    }

    @PostMapping("/{codigo}/mapa-ajuste/atualizar")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva os ajustes feitos no mapa")
    public void salvarAjustesMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarAjustesRequest request) {
        subprocessoService.salvarAjustesMapa(codigo, request.competencias());
    }

    @PostMapping("/{codigo}/competencia")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Adiciona uma competência ao mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> adicionarCompetencia(
            @PathVariable Long codigo,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoService.adicionarCompetencia(codigo, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencia/{codCompetencia}")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Atualiza uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> atualizarCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoService.atualizarCompetencia(codigo, codCompetencia, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencia/{codCompetencia}/remover")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Remove uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> removerCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia) {
        Mapa mapa = subprocessoService.removerCompetencia(codigo, codCompetencia);
        return ResponseEntity.ok(mapa);
    }

    // ============================================================================================
    // VALIDACAO / SUGESTOES (Migrado de SubprocessoValidacaoController)
    // ============================================================================================

    @PostMapping("/{codigo}/apresentar-sugestoes")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'APRESENTAR_SUGESTOES')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codigo,
            @RequestBody @Valid TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.apresentarSugestoes(codigo, request.texto(), usuario);
    }

    @GetMapping("/{codigo}/sugestoes")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public Map<String, Object> obterSugestoes(@PathVariable Long codigo) {
        return subprocessoService.obterSugestoes(codigo);
    }

    @GetMapping("/{codigo}/historico-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoValidacao(@PathVariable Long codigo) {
        return subprocessoService.listarHistoricoValidacao(codigo);
    }

    @PostMapping("/{codigo}/validar-mapa")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'VALIDAR_MAPA')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public ResponseEntity<Void> validarMapa(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.validarMapa(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/devolver-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DEVOLVER_MAPA')")
    @Operation(summary = "Devolve o mapa para ajuste (pelo chefe/gestor)")
    public ResponseEntity<Void> devolverValidacao(
            @PathVariable Long codigo,
            @RequestBody @Valid JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.devolverValidacao(codigo, request.justificativa(), usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita a validação (pelo gestor)")
    public ResponseEntity<Void> aceitarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.aceitarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/homologar-validacao")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa a validação")
    public ResponseEntity<Void> homologarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.homologarValidacao(codigo, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/submeter-mapa-ajustado")
    @PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Submete o mapa após ajustes solicitados")
    public ResponseEntity<Void> submeterMapaAjustado(
            @PathVariable Long codigo,
            @Valid @RequestBody SubmeterMapaAjustadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.submeterMapaAjustado(codigo, request, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/aceitar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.aceitarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codigo}/homologar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.homologarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    // ============================================================================================
    // ANALISES (Migrado de AnaliseController)
    // ============================================================================================

    @PostMapping("/{codigo}/analises-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de cadastro")
    public AnaliseHistoricoDto criarAnaliseCadastro(@PathVariable Long codigo,
                                                    @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(codigo, request, TipoAnalise.CADASTRO);
    }

    @PostMapping("/{codigo}/analises-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long codigo,
                                                     @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(codigo, request, TipoAnalise.VALIDACAO);
    }

    private AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        // UtilSanitizacao not strictly needed if stripped, but good practice. Assuming StringUtils logic:
        String obs = request.observacoes() != null ? request.observacoes().trim() : "";

        CriarAnaliseCommand criarAnaliseCommand = CriarAnaliseCommand.builder()
                .codSubprocesso(codSubprocesso)
                .observacoes(obs)
                .tipo(tipo)
                .acao(request.acao())
                .siglaUnidade(request.siglaUnidade())
                .tituloUsuario(request.tituloUsuario())
                .motivo(request.motivo())
                .build();

        Analise analise = subprocessoService.criarAnalise(sp, criarAnaliseCommand);
        return subprocessoService.paraHistoricoDto(analise);
    }
}
