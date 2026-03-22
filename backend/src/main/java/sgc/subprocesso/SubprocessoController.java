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
import sgc.comum.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
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
@PreAuthorize("isAuthenticated()")
public class SubprocessoController {

    private final SubprocessoService subprocessoService;
    private final SubprocessoTransicaoService transicaoService;
    private final UnidadeService unidadeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public List<Subprocesso> listar() {
        return subprocessoService.listarTodos();
    }

    @GetMapping("/{codSubprocesso}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public SubprocessoDetalheResponse obterPorCodigo(@PathVariable Long codSubprocesso, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.obterDetalhes(codSubprocesso, usuario);
    }

    @GetMapping("/{codSubprocesso}/status")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém apenas o status atual do subprocesso")
    public ResponseEntity<SubprocessoSituacaoDto> obterStatus(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(subprocessoService.obterStatus(codSubprocesso));
    }

    @GetMapping("/buscar")
    @PostAuthorize("hasPermission(returnObject.body, 'VISUALIZAR_SUBPROCESSO')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> buscarPorProcessoEUnidade(
            @RequestParam Long codProcesso, @RequestParam String siglaUnidade) {
        Unidade unidade = unidadeService.buscarPorSigla(siglaUnidade);
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

    @PostMapping("/{codSubprocesso}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> atualizar(
            @PathVariable Long codSubprocesso, @Valid @RequestBody AtualizarSubprocessoRequest request) {
        var atualizado = subprocessoService.atualizarEntidade(codSubprocesso, request);
        return ResponseEntity.ok(atualizado);
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

    @PostMapping("/{codSubprocesso}/reabrir-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre o cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirCadastro(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid JustificativaRequest request) {
        transicaoService.reabrirCadastro(codSubprocesso, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/reabrir-revisao-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre a revisão de cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid JustificativaRequest request) {
        transicaoService.reabrirRevisaoCadastro(codSubprocesso, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{codSubprocesso}/historico-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long codSubprocesso) {
        return subprocessoService.listarHistoricoCadastro(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/contexto-edicao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicao(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(subprocessoService.obterContextoEdicao(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/atividades-importacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'CONSULTAR_PARA_IMPORTACAO')")
    @Operation(summary = "Lista todas as atividades de um subprocesso finalizado para importação")
    public ResponseEntity<List<AtividadeDto>> listarAtividadesParaImportacao(@PathVariable Long codSubprocesso) {

        return ResponseEntity.ok(subprocessoService.listarAtividadesParaImportacao(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/validar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Valida se o cadastro está pronto para disponibilização")
    public ResponseEntity<ValidacaoCadastroDto> validarCadastro(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(subprocessoService.validarCadastro(codSubprocesso));
    }

    @JsonView(MapaViews.Publica.class)
    @GetMapping("/{codSubprocesso}/cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public Subprocesso obterCadastro(@PathVariable Long codSubprocesso) {
        return subprocessoService.buscarSubprocesso(codSubprocesso);
    }

    @PostMapping("/{codSubprocesso}/cadastro/disponibilizar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_CADASTRO')")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(
            @PathVariable Long codSubprocesso,
            @AuthenticationPrincipal Usuario usuario) {
        transicaoService.disponibilizarCadastro(codSubprocesso, usuario);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{codSubprocesso}/disponibilizar-revisao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_REVISAO_CADASTRO')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(
            @PathVariable Long codSubprocesso, @AuthenticationPrincipal Usuario usuario) {
        transicaoService.disponibilizarRevisao(codSubprocesso, usuario);

        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro disponibilizada"));
    }

    @PostMapping("/{codSubprocesso}/devolver-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_CADASTRO')")
    @Operation(summary = "Devolve o cadastro de atividades para the responsável")
    public void devolverCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        String sanitizedObservacoes = Optional.ofNullable(request.justificativa())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.devolverCadastro(codSubprocesso, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/aceitar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.aceitarCadastro(codSubprocesso, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/homologar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.homologarCadastro(codSubprocesso, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_REVISAO_CADASTRO')")
    public void devolverRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.justificativa())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.devolverRevisaoCadastro(codSubprocesso, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_REVISAO_CADASTRO')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.aceitarRevisaoCadastro(codSubprocesso, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_REVISAO_CADASTRO')")
    public void homologarRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.homologarRevisaoCadastro(codSubprocesso, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/importar-atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'IMPORTAR_ATIVIDADES')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long codSubprocesso, @RequestBody @Valid ImportarAtividadesRequest request) {
        boolean temDuplicatas = subprocessoService.importarAtividades(codSubprocesso, request.codSubprocessoOrigem(), request.codigosAtividades());
        if (temDuplicatas) {
            return Map.of("message", "Atividades importadas.", "aviso", SgcMensagens.IMPORTACAO_ATIVIDADES_DUPLICADAS);
        }
        return Map.of("message", "Atividades importadas.");
    }

    @PostMapping("/{codSubprocesso}/aceitar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@PathVariable Long codSubprocesso,
                                       @RequestBody @Valid ProcessarEmBlocoRequest request,
                                       @AuthenticationPrincipal Usuario usuario) {
        transicaoService.aceitarCadastroEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codSubprocesso}/homologar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@PathVariable Long codSubprocesso,
                                         @RequestBody @Valid ProcessarEmBlocoRequest request,
                                         @AuthenticationPrincipal Usuario usuario) {
        transicaoService.homologarCadastroEmBloco(request.subprocessos(), usuario);
    }

    @GetMapping("/{codSubprocesso}/impactos-mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VERIFICAR_IMPACTOS')")
    @JsonView(MapaViews.Publica.class)
    public ImpactoMapaResponse verificarImpactos(@PathVariable Long codSubprocesso, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.verificarImpactos(codSubprocesso, usuario);
    }

    @GetMapping("/{codSubprocesso}/mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @JsonView(MapaViews.Publica.class)
    public Mapa obterMapa(@PathVariable Long codSubprocesso) {
        Subprocesso sp = subprocessoService.buscarSubprocessoComMapa(codSubprocesso);
        return sp.getMapa();
    }

    @PostMapping("/{codSubprocesso}/disponibilizar-mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza o mapa para validação")
    public ResponseEntity<MensagemResponse> disponibilizarMapa(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody DisponibilizarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        transicaoService.disponibilizarMapa(codSubprocesso, request, usuario);
        return ResponseEntity.ok(new MensagemResponse("Mapa de competências disponibilizado."));
    }

    @GetMapping("/{codSubprocesso}/mapa-visualizacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa formatado para visualização")
    @JsonView(MapaViews.Publica.class)
    public MapaVisualizacaoResponse obterMapaParaVisualizacao(@PathVariable Long codSubprocesso) {
        return subprocessoService.mapaParaVisualizacao(codSubprocesso);
    }

    @PostMapping("/{codSubprocesso}/mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva as alterações do mapa")
    @JsonView(MapaViews.Publica.class)
    public Mapa salvarMapa(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody SalvarMapaRequest request) {
        return subprocessoService.salvarMapa(codSubprocesso, request);
    }

    @GetMapping("/{codSubprocesso}/mapa-completo")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa completo para edição/visualização")
    @JsonView(MapaViews.Publica.class)
    @Transactional(readOnly = true)
    public ResponseEntity<Mapa> obterMapaCompleto(@PathVariable Long codSubprocesso) {
        try {
            Mapa mapa = subprocessoService.mapaCompletoPorSubprocesso(codSubprocesso);
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            log.error("Erro ao buscar mapa completo para subprocesso {}: {}", codSubprocesso, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{codSubprocesso}/mapa-completo")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva o mapa completo (batch)")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> salvarMapaCompleto(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody SalvarMapaRequest request) {

        Mapa mapa = subprocessoService.salvarMapa(codSubprocesso, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codSubprocesso}/disponibilizar-mapa-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza mapas em bloco")
    public void disponibilizarMapaEmBloco(@PathVariable Long codSubprocesso,
                                          @RequestBody @Valid ProcessarEmBlocoRequest request,
                                          @AuthenticationPrincipal Usuario usuario) {
        DisponibilizarMapaRequest dispoReq = DisponibilizarMapaRequest.builder()
                .dataLimite(request.dataLimite())
                .build();
        transicaoService.disponibilizarMapaEmBloco(request.subprocessos(), dispoReq, usuario);
    }

    @GetMapping("/{codSubprocesso}/mapa-ajuste")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Obtém dados do mapa preparados para ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long codSubprocesso) {
        return subprocessoService.obterMapaParaAjuste(codSubprocesso);
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
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> adicionarCompetencia(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoService.adicionarCompetencia(codSubprocesso, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codSubprocesso}/competencia/{codCompetencia}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Atualiza uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> atualizarCompetencia(
            @PathVariable Long codSubprocesso,
            @PathVariable Long codCompetencia,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoService.atualizarCompetencia(codSubprocesso, codCompetencia, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codSubprocesso}/competencia/{codCompetencia}/remover")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Remove uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> removerCompetencia(
            @PathVariable Long codSubprocesso,
            @PathVariable Long codCompetencia) {
        Mapa mapa = subprocessoService.removerCompetencia(codSubprocesso, codCompetencia);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codSubprocesso}/apresentar-sugestoes")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'APRESENTAR_SUGESTOES')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        transicaoService.apresentarSugestoes(codSubprocesso, request.texto(), usuario);
    }

    @GetMapping("/{codSubprocesso}/sugestoes")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public Map<String, Object> obterSugestoes(@PathVariable Long codSubprocesso) {
        return subprocessoService.obterSugestoes(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/historico-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoValidacao(@PathVariable Long codSubprocesso) {
        return subprocessoService.listarHistoricoValidacao(codSubprocesso);
    }

    @PostMapping("/{codSubprocesso}/validar-mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VALIDAR_MAPA')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public ResponseEntity<Void> validarMapa(@PathVariable Long codSubprocesso, @AuthenticationPrincipal Usuario usuario) {
        transicaoService.validarMapa(codSubprocesso, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/devolver-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_MAPA')")
    @Operation(summary = "Devolve o mapa para ajuste (pelo chefe/gestor)")
    public ResponseEntity<Void> devolverValidacao(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        transicaoService.devolverValidacao(codSubprocesso, request.justificativa(), usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/aceitar-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita a validação (pelo gestor)")
    public ResponseEntity<Void> aceitarValidacao(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String observacoes = Optional.ofNullable(request)
                .map(TextoOpcionalRequest::texto)
                .map(UtilSanitizacao::sanitizar)
                .orElse(null);
        transicaoService.aceitarValidacao(codSubprocesso, observacoes, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/homologar-validacao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa a validação")
    public ResponseEntity<Void> homologarValidacao(
            @PathVariable Long codSubprocesso,
            @RequestBody(required = false) TextoOpcionalRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String observacoes = Optional.ofNullable(request)
                .map(TextoOpcionalRequest::texto)
                .map(UtilSanitizacao::sanitizar)
                .orElse(null);
        transicaoService.homologarValidacao(codSubprocesso, observacoes, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/submeter-mapa-ajustado")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Submete o mapa após ajustes solicitados")
    public ResponseEntity<Void> submeterMapaAjustado(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody SubmeterMapaAjustadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        transicaoService.submeterMapaAjustado(codSubprocesso, request, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codSubprocesso}/aceitar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long codSubprocesso,
                                        @RequestBody @Valid ProcessarEmBlocoRequest request,
                                        @AuthenticationPrincipal Usuario usuario) {
        transicaoService.aceitarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codSubprocesso}/homologar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long codSubprocesso,
                                          @RequestBody @Valid ProcessarEmBlocoRequest request,
                                          @AuthenticationPrincipal Usuario usuario) {
        transicaoService.homologarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codSubprocesso}/analises-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de cadastro")
    public AnaliseHistoricoDto criarAnaliseCadastro(@PathVariable Long codSubprocesso,
                                                    @RequestBody @Valid CriarAnaliseRequest request,
                                                    @AuthenticationPrincipal Usuario usuario) {
        return criarAnalise(codSubprocesso, request, TipoAnalise.CADASTRO, usuario);
    }

    @PostMapping("/{codSubprocesso}/analises-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long codSubprocesso,
                                                     @RequestBody @Valid CriarAnaliseRequest request,
                                                     @AuthenticationPrincipal Usuario usuario) {
        return criarAnalise(codSubprocesso, request, TipoAnalise.VALIDACAO, usuario);
    }

    private AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        Analise analise = transicaoService.criarAnalise(sp, request, tipo, usuario);
        return subprocessoService.paraHistoricoDto(analise);
    }
}
