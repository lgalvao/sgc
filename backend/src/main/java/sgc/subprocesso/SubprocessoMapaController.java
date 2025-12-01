package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesReq;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoMapaService;
import sgc.subprocesso.service.SubprocessoMapaWorkflowService;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoMapaController {
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
     * @param codigo O código do subprocesso.
     * @param request        O DTO contendo as alterações do mapa.
     * @param usuario        O usuário autenticado que está salvando o mapa.
     * @return O {@link MapaCompletoDto} representando o estado atualizado do mapa.
     */
    @PostMapping("/{codigo}/mapa/atualizar")
    @Transactional
    public MapaCompletoDto salvarMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return subprocessoMapaWorkflowService.salvarMapaSubprocesso(codigo, request, usuario.getTituloEleitoral());
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
     * @param codigo O código do subprocesso.
     * @param request        O DTO contendo as competências ajustadas.
     * @param usuario        O usuário autenticado que está salvando os ajustes.
     */
    @PostMapping("/{codigo}/mapa-ajuste/atualizar")
    @Transactional
    public void salvarAjustesMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarAjustesReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoMapaService.salvarAjustesMapa(
                codigo,
                request.getCompetencias(),
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
     * @param codigo O código do subprocesso.
     * @param request        O DTO com a estrutura completa do mapa a ser salvo.
     * @param usuario        O usuário autenticado que realiza a operação.
     * @return Um {@link ResponseEntity} com o {@link MapaCompletoDto} atualizado.
     */
    @PostMapping("/{codigo}/mapa-completo/atualizar")
    @Transactional
    @Operation(summary = "Salva um mapa completo com competências e atividades")
    public ResponseEntity<MapaCompletoDto> salvarMapaCompleto(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.salvarMapaSubprocesso(codigo, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencias")
    @Transactional
    @Operation(summary = "Adiciona uma nova competência a um mapa")
    public ResponseEntity<MapaCompletoDto> adicionarCompetencia(
            @PathVariable Long codigo,
            @RequestBody @Valid CompetenciaReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.adicionarCompetencia(codigo, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }

    @PutMapping("/{codigo}/competencias/{codCompetencia}")
    @Transactional
    @Operation(summary = "Atualiza uma competência existente em um mapa")
    public ResponseEntity<MapaCompletoDto> atualizarCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia,
            @RequestBody @Valid CompetenciaReq request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.atualizarCompetencia(codigo, codCompetencia, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }

    @DeleteMapping("/{codigo}/competencias/{codCompetencia}")
    @Transactional
    @Operation(summary = "Remove uma competência de um mapa")
    public ResponseEntity<MapaCompletoDto> removerCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia,
            @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = subprocessoMapaWorkflowService.removerCompetencia(codigo, codCompetencia, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/disponibilizar")
    @Transactional
    @Operation(summary = "Disponibiliza o mapa de competências para validação")
    public ResponseEntity<Void> disponibilizarMapa(
        @PathVariable Long codigo,
        @RequestBody @Valid DisponibilizarMapaRequest request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoMapaWorkflowService.disponibilizarMapa(codigo, request, usuario);
        return ResponseEntity.ok().build();
    }
}
