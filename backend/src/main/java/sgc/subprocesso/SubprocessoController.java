package sgc.subprocesso;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.ComumDtos.*;
import sgc.comum.*;
import sgc.mapa.dto.*;
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

    private final SubprocessoConsultaService consultaService;
    private final AnaliseHistoricoService analiseHistoricoService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoTransicaoService transicaoService;
    private final UnidadeService unidadeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SubprocessoListagemDto> listar() {
        return consultaService.listarTodos().stream()
                .map(SubprocessoListagemDto::fromEntity)
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
        return ResponseEntity.created(uri).body(SubprocessoResumoDto.fromEntity(subprocessoCriado));
    }

    @PostMapping("/{codSubprocesso}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubprocessoResumoDto> atualizar(
            @PathVariable Long codSubprocesso, @Valid @RequestBody AtualizarSubprocessoRequest request) {
        subprocessoService.atualizarEntidade(codSubprocesso, request.paraCommand());
        var subprocessoAtualizado = consultaService.buscarSubprocesso(codSubprocesso);
        return ResponseEntity.ok(SubprocessoResumoDto.fromEntity(subprocessoAtualizado));
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
        return consultaService.listarHistoricoCadastro(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/contexto-edicao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicao(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterContextoEdicao(codSubprocesso));
    }

    @GetMapping("/{codSubprocesso}/contexto-cadastro-atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoCadastroAtividadesResponse> obterContextoCadastroAtividades(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(consultaService.obterContextoCadastroAtividades(codSubprocesso));
    }

    @GetMapping("/contexto-edicao/buscar")
    @PostAuthorize("hasPermission(returnObject.body.detalhes.subprocesso.codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicaoPorProcessoEUnidade(
            @RequestParam Long codProcesso,
            @RequestParam String siglaUnidade
    ) {
        Long codUnidade = unidadeService.buscarCodigoPorSigla(siglaUnidade);
        Subprocesso subprocesso = consultaService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
        return ResponseEntity.ok(consultaService.obterContextoEdicao(subprocesso.getCodigo()));
    }

    @GetMapping("/contexto-cadastro-atividades/buscar")
    @PostAuthorize("hasPermission(returnObject.body.detalhes.subprocesso.codigo, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public ResponseEntity<ContextoCadastroAtividadesResponse> obterContextoCadastroAtividadesPorProcessoEUnidade(
            @RequestParam Long codProcesso,
            @RequestParam String siglaUnidade
    ) {
        Long codUnidade = unidadeService.buscarCodigoPorSigla(siglaUnidade);
        Subprocesso subprocesso = consultaService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
        return ResponseEntity.ok(consultaService.obterContextoCadastroAtividades(subprocesso.getCodigo()));
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
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(
            @PathVariable Long codSubprocesso) {
        transicaoService.disponibilizarCadastro(codSubprocesso);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{codSubprocesso}/iniciar-revisao-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_CADASTRO')")
    @Operation(summary = "Inicia a revisão do cadastro de atividades (transiciona de NAO_INICIADO para REVISAO_CADASTRO_EM_ANDAMENTO)")
    public ResponseEntity<MensagemResponse> iniciarRevisaoCadastro(@PathVariable Long codSubprocesso) {
        transicaoService.iniciarRevisaoCadastro(codSubprocesso);
        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro iniciada"));
    }

    @PostMapping("/{codSubprocesso}/cancelar-inicio-revisao-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_CADASTRO')")
    @Operation(summary = "Cancela o início da revisão do cadastro de atividades (transiciona de REVISAO_CADASTRO_EM_ANDAMENTO para NAO_INICIADO)")
    public ResponseEntity<MensagemResponse> cancelarInicioRevisaoCadastro(@PathVariable Long codSubprocesso) {
        transicaoService.cancelarInicioRevisaoCadastro(codSubprocesso);
        return ResponseEntity.ok(new MensagemResponse("Início da revisão do cadastro cancelado"));
    }

    @PostMapping("/{codSubprocesso}/disponibilizar-revisao")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DISPONIBILIZAR_REVISAO_CADASTRO')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(
            @PathVariable Long codSubprocesso) {
        transicaoService.disponibilizarRevisao(codSubprocesso);

        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro disponibilizada"));
    }

    @PostMapping("/{codSubprocesso}/devolver-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_CADASTRO')")
    @Operation(summary = "Devolve o cadastro de atividades para the responsável")
    public void devolverCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody JustificativaRequest request) {

        String sanitizedObservacoes = Optional.ofNullable(request.justificativa())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.devolverCadastro(codSubprocesso, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/aceitar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.aceitarCadastro(codSubprocesso, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/homologar-cadastro")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request) {

        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.homologarCadastro(codSubprocesso, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'DEVOLVER_REVISAO_CADASTRO')")
    public void devolverRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody JustificativaRequest request) {
        String sanitizedObservacoes = Optional.ofNullable(request.justificativa())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.devolverRevisaoCadastro(codSubprocesso, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'ACEITAR_REVISAO_CADASTRO')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.aceitarRevisaoCadastro(codSubprocesso, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'HOMOLOGAR_REVISAO_CADASTRO')")
    public void homologarRevisaoCadastro(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody TextoOpcionalRequest request) {
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        transicaoService.homologarRevisaoCadastro(codSubprocesso, sanitizedObservacoes);
    }

    @PostMapping("/{codSubprocesso}/importar-atividades")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'IMPORTAR_ATIVIDADES')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long codSubprocesso, @RequestBody @Valid ImportarAtividadesRequest request) {
        boolean temDuplicatas = subprocessoService.importarAtividades(codSubprocesso, request.codSubprocessoOrigem(), request.codigosAtividades());
        if (temDuplicatas) {
            return Map.of("message", "Atividades importadas.", "aviso", Mensagens.IMPORTACAO_ATIVIDADES_DUPLICADAS);
        }
        return Map.of("message", "Atividades importadas.");
    }

    @PostMapping("/{codSubprocesso}/aceitar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@PathVariable Long codSubprocesso,
                                       @RequestBody @Valid ProcessarEmBlocoRequest request) {
        transicaoService.aceitarCadastroEmBloco(request.subprocessos());
    }

    @PostMapping("/{codSubprocesso}/homologar-cadastro-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@PathVariable Long codSubprocesso,
                                         @RequestBody @Valid ProcessarEmBlocoRequest request) {
        transicaoService.homologarCadastroEmBloco(request.subprocessos());
    }

    @GetMapping("/{codSubprocesso}/impactos-mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VERIFICAR_IMPACTOS')")
    public ImpactoMapaResponse verificarImpactos(@PathVariable Long codSubprocesso) {
        return consultaService.verificarImpactos(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    public MapaCompletoDto obterMapa(@PathVariable Long codSubprocesso) {
        return consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso);
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

    @PostMapping("/{codSubprocesso}/mapa")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Salva as alterações do mapa")
    public MapaCompletoDto salvarMapa(
            @PathVariable Long codSubprocesso,
            @Valid @RequestBody SalvarMapaRequest request) {
        subprocessoService.salvarMapa(codSubprocesso, request);
        return consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso);
    }

    @GetMapping("/{codSubprocesso}/mapa-completo")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém o mapa completo para edição/visualização")
    @Transactional(readOnly = true)
    public ResponseEntity<MapaCompletoDto> obterMapaCompleto(@PathVariable Long codSubprocesso) {
        try {
            return ResponseEntity.ok(consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso));
        } catch (Exception e) {
            log.error("Erro ao buscar mapa completo para subprocesso {}: {}", codSubprocesso, e.getMessage(), e);
            throw e;
        }
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

    @PostMapping("/{codSubprocesso}/disponibilizar-mapa-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'DISPONIBILIZAR_MAPA')")
    @Operation(summary = "Disponibiliza mapas em bloco")
    public void disponibilizarMapaEmBloco(@PathVariable Long codSubprocesso,
                                          @RequestBody @Valid ProcessarEmBlocoRequest request) {
        DisponibilizarMapaRequest dispoReq = DisponibilizarMapaRequest.builder()
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
            @Valid @RequestBody CompetenciaRequest request) {
        subprocessoService.adicionarCompetencia(codSubprocesso, request);
        return ResponseEntity.ok(consultaService.mapaCompletoDtoPorSubprocesso(codSubprocesso));
    }

    @PostMapping("/{codSubprocesso}/competencia/{codCompetencia}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'EDITAR_MAPA')")
    @Operation(summary = "Atualiza uma competência do mapa")
    public ResponseEntity<MapaCompletoDto> atualizarCompetencia(
            @PathVariable Long codSubprocesso,
            @PathVariable Long codCompetencia,
            @Valid @RequestBody CompetenciaRequest request) {
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
    public Map<String, Object> obterSugestoes(@PathVariable Long codSubprocesso) {
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

    private @org.jspecify.annotations.Nullable String sanitizarTextoOpcional(
            @org.jspecify.annotations.Nullable TextoOpcionalRequest request) {
        if (request == null || request.texto() == null) {
            return null;
        }
        return UtilSanitizacao.sanitizar(request.texto());
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

    @PostMapping("/{codSubprocesso}/aceitar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long codSubprocesso,
                                        @RequestBody @Valid ProcessarEmBlocoRequest request) {
        transicaoService.aceitarValidacaoEmBloco(request.subprocessos());
    }

    @PostMapping("/{codSubprocesso}/homologar-validacao-bloco")
    @PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'HOMOLOGAR_MAPA')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long codSubprocesso,
                                          @RequestBody @Valid ProcessarEmBlocoRequest request) {
        transicaoService.homologarValidacaoEmBloco(request.subprocessos());
    }

    @PostMapping("/{codSubprocesso}/analises-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de cadastro")
    public AnaliseHistoricoDto criarAnaliseCadastro(@PathVariable Long codSubprocesso,
                                                    @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(codSubprocesso, request, TipoAnalise.CADASTRO);
    }

    @PostMapping("/{codSubprocesso}/analises-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma análise de validação")
    public AnaliseHistoricoDto criarAnaliseValidacao(@PathVariable Long codSubprocesso,
                                                     @RequestBody @Valid CriarAnaliseRequest request) {
        return criarAnalise(codSubprocesso, request, TipoAnalise.VALIDACAO);
    }

    private AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        Analise analise = transicaoService.criarAnalise(sp, request, tipo);
        return analiseHistoricoService.converter(analise);
    }
}
