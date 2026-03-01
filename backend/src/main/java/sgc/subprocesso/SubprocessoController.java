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
    private final OrganizacaoFacade organizacaoFacade;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public List<Subprocesso> listar() {
        return subprocessoService.listarEntidades();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public SubprocessoDetalheResponse obterPorId(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.obterDetalhes(id, usuario);
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém apenas o status atual do subprocesso")
    public ResponseEntity<SubprocessoSituacaoDto> obterStatus(@PathVariable Long id) {
        return ResponseEntity.ok(subprocessoService.obterStatus(id));
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

    @PostMapping("/{id}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<Subprocesso> atualizar(
            @PathVariable Long id, @Valid @RequestBody AtualizarSubprocessoRequest request) {
        var atualizado = subprocessoService.atualizarEntidade(id, request);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{id}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        subprocessoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/data-limite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarDataLimite(
            @PathVariable Long id,
            @RequestBody @Valid DataRequest request) {
        subprocessoService.alterarDataLimite(id, request.data());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reabrir-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre o cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirCadastro(
            @PathVariable Long id,
            @RequestBody @Valid JustificativaRequest request) {
        subprocessoService.reabrirCadastro(id, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reabrir-revisao-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reabre a revisão de cadastro de um subprocesso")
    public ResponseEntity<Void> reabrirRevisaoCadastro(
            @PathVariable Long id,
            @RequestBody @Valid JustificativaRequest request) {
        subprocessoService.reabrirRevisaoCadastro(id, request.justificativa());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/historico-cadastro")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long id) {
        return subprocessoService.listarHistoricoCadastro(id);
    }

    @GetMapping("/{id}/contexto-edicao")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicao(@PathVariable Long id) {
        return ResponseEntity.ok(subprocessoService.obterContextoEdicao(id));
    }

    @GetMapping("/{id}/validar-cadastro")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Valida se o cadastro está pronto para disponibilização")
    public ResponseEntity<ValidacaoCadastroDto> validarCadastro(@PathVariable Long id) {
        return ResponseEntity.ok(subprocessoService.validarCadastro(id));
    }

    @JsonView(MapaViews.Publica.class)
    @GetMapping("/{id}/cadastro")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public Subprocesso obterCadastro(@PathVariable Long id) {
        return subprocessoService.buscarSubprocesso(id);
    }

    @PostMapping("/{id}/cadastro/disponibilizar")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'DISPONIBILIZAR_CADASTRO')")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.disponibilizarCadastro(id, usuario);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{id}/disponibilizar-revisao")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'DISPONIBILIZAR_REVISAO_CADASTRO')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(
            @PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.disponibilizarRevisao(id, usuario);

        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro de atividades disponibilizada"));
    }

    @PostMapping("/{id}/devolver-cadastro")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'DEVOLVER_CADASTRO')")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long id,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        String sanitizedObservacoes = Optional.ofNullable(request.justificativa())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.devolverCadastro(id, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{id}/aceitar-cadastro")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.aceitarCadastro(id, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{id}/homologar-cadastro")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.homologarCadastro(id, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{id}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'DEVOLVER_REVISAO_CADASTRO')")
    public void devolverRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.justificativa())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.devolverRevisaoCadastro(id, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{id}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'ACEITAR_REVISAO_CADASTRO')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.aceitarRevisaoCadastro(id, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{id}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'HOMOLOGAR_REVISAO_CADASTRO')")
    public void homologarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoService.homologarRevisaoCadastro(id, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{id}/importar-atividades")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'IMPORTAR_ATIVIDADES')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long id, @RequestBody @Valid ImportarAtividadesRequest request) {
        subprocessoService.importarAtividades(id, request.codSubprocessoOrigem());
        return Map.of("message", "Atividades importadas.");
    }

    @PostMapping("/{id}/aceitar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@PathVariable Long id,
                                       @RequestBody @Valid ProcessarEmBlocoRequest request,
                                       @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.aceitarCadastroEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{id}/homologar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@PathVariable Long id,
                                         @RequestBody @Valid ProcessarEmBlocoRequest request,
                                         @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.homologarCadastroEmBloco(request.subprocessos(), usuario);
    }

    @GetMapping("/{id}/impactos-mapa")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VERIFICAR_IMPACTOS')")
    @JsonView(MapaViews.Publica.class)
    public ImpactoMapaResponse verificarImpactos(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.verificarImpactos(id, usuario);
    }

    @GetMapping("/{id}/mapa")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @JsonView(MapaViews.Publica.class)
    public Mapa obterMapa(@PathVariable Long id) {
        Subprocesso sp = subprocessoService.buscarSubprocessoComMapa(id);
        return sp.getMapa();
    }

    @PostMapping("/{id}/disponibilizar-mapa")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza o mapa para validação")
    public ResponseEntity<MensagemResponse> disponibilizarMapa(
            @PathVariable Long id,
            @Valid @RequestBody DisponibilizarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.disponibilizarMapa(id, request, usuario);
        return ResponseEntity.ok(new MensagemResponse("Mapa de competências disponibilizado."));
    }

    @GetMapping("/{id}/mapa-visualizacao")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa formatado para visualização")
    @JsonView(MapaViews.Publica.class)
    public MapaVisualizacaoResponse obterMapaParaVisualizacao(@PathVariable Long id) {
        return subprocessoService.mapaParaVisualizacao(id);
    }

    @PostMapping("/{id}/mapa")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva as alterações do mapa")
    @JsonView(MapaViews.Publica.class)
    public Mapa salvarMapa(
            @PathVariable Long id,
            @Valid @RequestBody SalvarMapaRequest request) {
        return subprocessoService.salvarMapa(id, request);
    }

    @GetMapping("/{id}/mapa-completo")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa completo para edição/visualização")
    @JsonView(MapaViews.Publica.class)
    @Transactional(readOnly = true)
    public ResponseEntity<Mapa> obterMapaCompleto(@PathVariable Long id) {
        try {
            Mapa mapa = subprocessoService.mapaCompletoPorSubprocesso(id);
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            log.error("Erro ao buscar mapa completo para subprocesso {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{id}/mapa-completo")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva o mapa completo (batch)")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> salvarMapaCompleto(
            @PathVariable Long id,
            @Valid @RequestBody SalvarMapaRequest request) {

        Mapa mapa = subprocessoService.salvarMapa(id, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{id}/disponibilizar-mapa-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza mapas em bloco")
    public void disponibilizarMapaEmBloco(@PathVariable Long id,
                                          @RequestBody @Valid ProcessarEmBlocoRequest request,
                                          @AuthenticationPrincipal Usuario usuario) {
        DisponibilizarMapaRequest dispoReq = DisponibilizarMapaRequest.builder()
                .dataLimite(request.dataLimite())
                .build();
        subprocessoService.disponibilizarMapaEmBloco(request.subprocessos(), dispoReq, usuario);
    }

    @GetMapping("/{id}/mapa-ajuste")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Obtém dados do mapa preparados para ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long id) {
        return subprocessoService.obterMapaParaAjuste(id);
    }

    @PostMapping("/{id}/mapa-ajuste/atualizar")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva os ajustes feitos no mapa")
    public void salvarAjustesMapa(
            @PathVariable Long id,
            @RequestBody @Valid SalvarAjustesRequest request) {
        subprocessoService.salvarAjustesMapa(id, request.competencias());
    }

    @PostMapping("/{id}/competencia")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Adiciona uma competência ao mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> adicionarCompetencia(
            @PathVariable Long id,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoService.adicionarCompetencia(id, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{id}/competencia/{codCompetencia}")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Atualiza uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> atualizarCompetencia(
            @PathVariable Long id,
            @PathVariable Long codCompetencia,
            @Valid @RequestBody CompetenciaRequest request) {
        Mapa mapa = subprocessoService.atualizarCompetencia(id, codCompetencia, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{id}/competencia/{codCompetencia}/remover")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Remove uma competência do mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> removerCompetencia(
            @PathVariable Long id,
            @PathVariable Long codCompetencia) {
        Mapa mapa = subprocessoService.removerCompetencia(id, codCompetencia);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{id}/apresentar-sugestoes")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'APRESENTAR_SUGESTOES')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long id,
            @RequestBody @Valid TextoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.apresentarSugestoes(id, request.texto(), usuario);
    }

    @GetMapping("/{id}/sugestoes")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public Map<String, Object> obterSugestoes(@PathVariable Long id) {
        return subprocessoService.obterSugestoes();
    }

    @GetMapping("/{id}/historico-validacao")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public List<AnaliseHistoricoDto> obterHistoricoValidacao(@PathVariable Long id) {
        return subprocessoService.listarHistoricoValidacao(id);
    }

    @PostMapping("/{id}/validar-mapa")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'VALIDAR_MAPA')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public ResponseEntity<Void> validarMapa(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.validarMapa(id, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/devolver-validacao")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'DEVOLVER_MAPA')")
    @Operation(summary = "Devolve o mapa para ajuste (pelo chefe/gestor)")
    public ResponseEntity<Void> devolverValidacao(
            @PathVariable Long id,
            @RequestBody @Valid JustificativaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.devolverValidacao(id, request.justificativa(), usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/aceitar-validacao")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita a validação (pelo gestor)")
    public ResponseEntity<Void> aceitarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.aceitarValidacao(id, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/homologar-validacao")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa a validação")
    public ResponseEntity<Void> homologarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.homologarValidacao(id, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/submeter-mapa-ajustado")
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'AJUSTAR_MAPA')")
    @Operation(summary = "Submete o mapa após ajustes solicitados")
    public ResponseEntity<Void> submeterMapaAjustado(
            @PathVariable Long id,
            @Valid @RequestBody SubmeterMapaAjustadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.submeterMapaAjustado(id, request, usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/aceitar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long id,
                                        @RequestBody @Valid ProcessarEmBlocoRequest request,
                                        @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.aceitarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{id}/homologar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long id,
                                          @RequestBody @Valid ProcessarEmBlocoRequest request,
                                          @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.homologarValidacaoEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{id}/analises-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de cadastro")
    public AnaliseHistoricoDto criarAnaliseCadastro(@PathVariable Long id,
                                                    @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(id, request, TipoAnalise.CADASTRO);
    }

    @PostMapping("/{id}/analises-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long id,
                                                     @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(id, request, TipoAnalise.VALIDACAO);
    }

    private AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        Analise analise = subprocessoService.criarAnalise(sp, request, tipo);
        return subprocessoService.paraHistoricoDto(analise);
    }
}
