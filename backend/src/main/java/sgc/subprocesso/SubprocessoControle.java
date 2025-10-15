package sgc.subprocesso;

import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.analise.modelo.TipoAnalise;
import sgc.atividade.modelo.Atividade;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.MapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.sgrh.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import sgc.subprocesso.dto.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoControle {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private final SubprocessoService subprocessoService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final SubprocessoWorkflowService subprocessoWorkflowService;
    private final SubprocessoMapaService subprocessoMapaService;
    private final MapaService mapaService;
    private final sgc.mapa.MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;
    private final sgc.analise.AnaliseService analiseService;
    private final sgc.analise.dto.AnaliseMapper analiseMapper;

    @GetMapping
    public List<SubprocessoDto> listar() {
        return subprocessoDtoService.listar();
    }

    @GetMapping("/{id}")
    public SubprocessoDetalheDto obterPorId(@PathVariable Long id,
                                              @RequestParam(required = false) String perfil,
                                              @RequestParam(required = false) Long unidadeUsuario) {
        return subprocessoDtoService.obterDetalhes(id, perfil, unidadeUsuario);
    }

    @GetMapping("/{id}/historico-cadastro")
    public List<sgc.analise.dto.AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.CADASTRO)
            .stream()
            .map(analiseMapper::toAnaliseHistoricoDto)
            .toList();
    }

    @PostMapping("/{id}/disponibilizar")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<RespostaDto> disponibilizarCadastro(
        @PathVariable("id") Long subprocessoId,
        @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoWorkflowService.disponibilizarCadastro(subprocessoId, usuario);
        return ResponseEntity.ok(new RespostaDto("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{id}/disponibilizar-revisao")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<RespostaDto> disponibilizarRevisao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(id);
        if (faltando != null && !faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("id", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.", Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoWorkflowService.disponibilizarRevisao(id, usuario);
        return ResponseEntity.ok(new RespostaDto("Revisão do cadastro de atividades disponibilizada"));
    }

    @GetMapping("/{id}/cadastro")
    public SubprocessoCadastroDto obterCadastro(@PathVariable Long id) {
        return subprocessoDtoService.obterCadastro(id);
    }

    @PostMapping
    public ResponseEntity<SubprocessoDto> criar(@Valid @RequestBody SubprocessoDto subprocessoDto) {
        var salvo = subprocessoService.criar(subprocessoDto);
        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo.sanitize());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubprocessoDto> atualizar(@PathVariable Long id, @Valid @RequestBody SubprocessoDto subprocessoDto) {
        var atualizado = subprocessoService.atualizar(id, subprocessoDto);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{id}/devolver-cadastro")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedMotivo = HTML_SANITIZER_POLICY.sanitize(request.motivo());
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.devolverCadastro(
            id,
            sanitizedMotivo,
            sanitizedObservacoes,
            usuario
        );
    }

    @PostMapping("/{id}/aceitar-cadastro")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.aceitarCadastro(
            id,
            sanitizedObservacoes,
            usuario.getTituloEleitoral()
        );
    }

    @PostMapping("/{id}/homologar-cadastro")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.homologarCadastro(
            id,
            sanitizedObservacoes,
            usuario.getTituloEleitoral()
        );
    }

    @PostMapping("/{id}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    public void devolverRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedMotivo = HTML_SANITIZER_POLICY.sanitize(request.motivo());
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.devolverRevisaoCadastro(
            id,
            sanitizedMotivo,
            sanitizedObservacoes,
            usuario
        );
    }

    @PostMapping("/{id}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    public void aceitarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.aceitarRevisaoCadastro(
            id,
            sanitizedObservacoes,
            usuario
        );
    }

    @PostMapping("/{id}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    public void homologarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.homologarRevisaoCadastro(
            id,
            sanitizedObservacoes,
            usuario
        );
    }

    @GetMapping("/{id}/impactos-mapa")
    @Operation(summary = "Verifica os impactos da revisão no mapa de competências")
    public ImpactoMapaDto verificarImpactos(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return impactoMapaService.verificarImpactos(id, usuario);
    }
    
    @GetMapping("/{id}/mapa")
    public MapaCompletoDto obterMapa(@PathVariable Long id) {
        return mapaService.obterMapaSubprocesso(id);
    }

    @GetMapping("/{id}/mapa-visualizacao")
    public MapaVisualizacaoDto obterMapaVisualizacao(@PathVariable("id") Long subprocessoId) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(subprocessoId);
    }
    
    @PutMapping("/{id}/mapa")
    @Transactional
    public MapaCompletoDto salvarMapa(
        @PathVariable Long id,
        @RequestBody @Valid SalvarMapaRequest request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        return mapaService.salvarMapaSubprocesso(id, request, usuario.getTituloEleitoral());
    }
    
    @PostMapping("/{id}/disponibilizar-mapa")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disponibiliza o mapa de competências para as unidades")
    public ResponseEntity<RespostaDto> disponibilizarMapa(
            @PathVariable Long id,
            @RequestBody @Valid DisponibilizarMapaReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());

        subprocessoWorkflowService.disponibilizarMapa(
            id,
            sanitizedObservacoes,
            request.dataLimiteEtapa2(),
            usuario
        );
        return ResponseEntity.ok(new RespostaDto("Mapa de competências disponibilizado com sucesso."));
    }
    
    @PostMapping("/{id}/apresentar-sugestoes")
    @Transactional
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long id,
            @RequestBody @Valid ApresentarSugestoesReq request,
            @AuthenticationPrincipal Usuario usuario) {
        var sanitizedSugestoes = HTML_SANITIZER_POLICY.sanitize(request.sugestoes());

        subprocessoWorkflowService.apresentarSugestoes(
            id,
            sanitizedSugestoes,
            usuario.getTituloEleitoral()
        );
    }
    
    @PostMapping("/{id}/validar-mapa")
    @Transactional
    @Operation(summary = "Valida o mapa de competências da unidade")
    public void validarMapa(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.validarMapa(id, usuario.getTituloEleitoral());
    }

    @GetMapping("/{id}/sugestoes")
    public SugestoesDto obterSugestoes(@PathVariable Long id) {
        return subprocessoDtoService.obterSugestoes(id);
    }

    @GetMapping("/{id}/historico-validacao")
    public List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> obterHistoricoValidacao(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO)
            .stream()
            .map(analiseMapper::toAnaliseValidacaoHistoricoDto)
            .toList();
    }

    @PostMapping("/{id}/devolver-validacao")
    @Transactional
    @Operation(summary = "Devolve a validação do mapa para a unidade de negócio")
    public void devolverValidacao(
        @PathVariable Long id,
        @RequestBody @Valid DevolverValidacaoReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        var sanitizedJustificativa = HTML_SANITIZER_POLICY.sanitize(request.justificativa());

        subprocessoWorkflowService.devolverValidacao(
            id,
            sanitizedJustificativa,
            usuario
        );
    }

    @PostMapping("/{id}/aceitar-validacao")
    @Transactional
    @Operation(summary = "Aceita a validação do mapa")
    public void aceitarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.aceitarValidacao(id, usuario);
    }

    @PostMapping("/{id}/homologar-validacao")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa a validação do mapa")
    public void homologarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        subprocessoWorkflowService.homologarValidacao(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        subprocessoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/mapa-ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long id) {
        return subprocessoDtoService.obterMapaParaAjuste(id);
    }

    @PutMapping("/{id}/mapa-ajuste")
    @Transactional
    public void salvarAjustesMapa(
        @PathVariable Long id,
        @RequestBody @Valid SalvarAjustesReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoMapaService.salvarAjustesMapa(
            id,
            request.competencias(),
            usuario.getTituloEleitoral()
        );
    }

    @PostMapping("/{id}/submeter-mapa-ajustado")
    @Transactional
    @Operation(summary = "Submete o mapa ajustado para nova validação")
    public void submeterMapaAjustado(
        @PathVariable Long id,
        @RequestBody @Valid SubmeterMapaAjustadoReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());
        var sanitizedRequest = new SubmeterMapaAjustadoReq(sanitizedObservacoes, request.dataLimiteEtapa2());

        subprocessoWorkflowService.submeterMapaAjustado(id, sanitizedRequest, usuario.getTituloEleitoral());
    }

    @PostMapping("/{id}/importar-atividades")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
        @PathVariable Long id,
        @RequestBody @Valid ImportarAtividadesRequest request
    ) {
        subprocessoMapaService.importarAtividades(id, request.subprocessoOrigemId());
        return Map.of("message", "Atividades importadas com sucesso.");
    }
}