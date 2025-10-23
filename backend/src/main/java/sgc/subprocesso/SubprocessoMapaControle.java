package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.MapaService;
import sgc.mapa.MapaVisualizacaoService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.sgrh.Usuario;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesReq;
import sgc.subprocesso.modelo.Subprocesso;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoMapaControle {

    private final SubprocessoMapaService subprocessoMapaService;
    private final MapaService mapaService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;
    private final SubprocessoDtoService subprocessoDtoService;
    private final SubprocessoMapaWorkflowService subprocessoMapaWorkflowService;
    private final SubprocessoConsultaService subprocessoConsultaService;

    /**
     * Analisa e retorna os impactos de uma revisão de mapa de competências.
     * <p>
     * Compara o mapa em elaboração no subprocesso com o mapa vigente da unidade
     * para identificar atividades inseridas, removidas ou alteradas, e as
     * competências afetadas.
     *
     * @param codigo  O código do subprocesso em revisão.
     * @param usuario O usuário autenticado que realiza a verificação.
     * @return Um {@link ImpactoMapaDto} com o detalhamento dos impactos.
     */
    @GetMapping("/{codigo}/impactos-mapa")
    @Operation(summary = "Verifica os impactos da revisão no mapa de competências")
    public ImpactoMapaDto verificarImpactos(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        return impactoMapaService.verificarImpactos(codigo, usuario);
    }

    /**
     * Obtém a estrutura completa de um mapa associado a um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link MapaCompletoDto} com as competências e atividades do mapa.
     */
    @GetMapping("/{codigo}/mapa")
    public MapaCompletoDto obterMapa(@PathVariable Long codigo) {
        Subprocesso subprocesso = subprocessoConsultaService.getSubprocessoComMapa(codigo);
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codigo);
    }

    /**
     * Obtém uma representação aninhada e formatada do mapa de um subprocesso,
     * ideal para telas de visualização.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Um {@link MapaVisualizacaoDto} com a estrutura hierárquica completa do mapa.
     */
    @GetMapping("/{codigo}/mapa-visualizacao")
    public MapaVisualizacaoDto obterMapaVisualizacao(@PathVariable("codigo") Long codSubprocesso) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(codSubprocesso);
    }

    /**
     * Salva as alterações feitas no mapa de um subprocesso.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param request        O DTO contendo as alterações do mapa.
     * @param usuario        O usuário autenticado que está salvando o mapa.
     * @return O {@link MapaCompletoDto} representando o estado atualizado do mapa.
     */
    @PostMapping("/{codSubprocesso}/mapa/atualizar")
    @Transactional
    public MapaCompletoDto salvarMapa(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return subprocessoMapaWorkflowService.salvarMapaSubprocesso(codSubprocesso, request, usuario.getTituloEleitoral());
    }

    /**
     * Obtém os dados de um mapa para a tela de ajuste pós-validação.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link MapaAjusteDto} com os dados necessários para o ajuste.
     */
    @GetMapping("/{codigo}/mapa-ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long codigo) {
        return subprocessoDtoService.obterMapaParaAjuste(codigo);
    }

    /**
     * Salva os ajustes realizados em um mapa após a fase de validação.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param request        O DTO contendo as competências ajustadas.
     * @param usuario        O usuário autenticado que está salvando os ajustes.
     */
    @PostMapping("/{codSubprocesso}/mapa-ajuste/atualizar")
    @Transactional
    public void salvarAjustesMapa(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid SalvarAjustesReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoMapaService.salvarAjustesMapa(
                codSubprocesso,
                request.competencias(),
                usuario.getTituloEleitoral()
        );
    }

    /**
     * Obtém a estrutura completa de um mapa, incluindo competências e as
     * atividades associadas a cada uma.
     * <p>
     * Corresponde ao CDU-15.
     *
     * @param codigo código do subprocesso.
     * @return Um {@link ResponseEntity} com o {@link MapaCompletoDto}.
     */
    @GetMapping("/{codigo}/mapa-completo")
    @Operation(summary = "Obtém um mapa completo com competências e atividades (CDU-15)")
    public ResponseEntity<MapaCompletoDto> obterMapaCompleto(@PathVariable Long codigo) {
        Subprocesso subprocesso = subprocessoConsultaService.getSubprocessoComMapa(codigo);
        MapaCompletoDto mapa = mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codigo);
        return ResponseEntity.ok(mapa);
    }

    /**
     * Salva a estrutura completa de um mapa, incluindo a criação/edição de
     * competências e a atualização de seus vínculos com atividades.
     * <p>
     * Corresponde ao CDU-15.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param request        O DTO com a estrutura completa do mapa a ser salvo.
     * @param usuario        O usuário autenticado que realiza a operação.
     * @return Um {@link ResponseEntity} com o {@link MapaCompletoDto} atualizado.
     */
    @PostMapping("/{codSubprocesso}/mapa-completo/atualizar")
    @Transactional
    @Operation(summary = "Salva um mapa completo com competências e atividades")
    public ResponseEntity<MapaCompletoDto> salvarMapaCompleto(
            @PathVariable Long codSubprocesso,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.salvarMapaSubprocesso(codSubprocesso, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codSubprocesso}/competencias")
    @Transactional
    @Operation(summary = "Adiciona uma nova competência a um mapa")
    public ResponseEntity<MapaCompletoDto> adicionarCompetencia(
        @PathVariable Long codSubprocesso,
        @RequestBody @Valid CompetenciaReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.adicionarCompetencia(codSubprocesso, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }

    @PutMapping("/{codSubprocesso}/competencias/{competenciaId}")
    @Transactional
    @Operation(summary = "Atualiza uma competência existente em um mapa")
    public ResponseEntity<MapaCompletoDto> atualizarCompetencia(
        @PathVariable Long codSubprocesso,
        @PathVariable Long competenciaId,
        @RequestBody @Valid CompetenciaReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.atualizarCompetencia(codSubprocesso, competenciaId, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }

    @DeleteMapping("/{codSubprocesso}/competencias/{competenciaId}")
    @Transactional
    @Operation(summary = "Remove uma competência de um mapa")
    public ResponseEntity<MapaCompletoDto> removerCompetencia(
        @PathVariable Long codSubprocesso,
        @PathVariable Long competenciaId,
        @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.removerCompetencia(codSubprocesso, competenciaId, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }
}