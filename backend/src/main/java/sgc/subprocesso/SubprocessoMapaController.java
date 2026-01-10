package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoMapaController {
    private final SubprocessoFacade subprocessoFacade;
    private final MapaFacade mapaFacade;
    private final UsuarioService usuarioService;

    /**
     * Obtém o contexto completo para edição de mapa (BFF).
     *
     * @param codigo         O código do subprocesso.
     * @param perfil         O perfil do usuário (opcional).
     * @return O {@link ContextoEdicaoDto} com todos os dados necessários.
     */
    @GetMapping("/{codigo}/contexto-edicao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém o contexto completo para edição de mapa (BFF)")
    public ContextoEdicaoDto obterContextoEdicao(
            @PathVariable Long codigo,
            @RequestParam(required = false) sgc.organizacao.model.Perfil perfil) {
        return subprocessoFacade.obterContextoEdicao(codigo, perfil);
    }

    /**
     * Analisa e retorna os impactos de uma revisão de mapa de competências.
     *
     * <p>Compara o mapa em elaboração no subprocesso com o mapa vigente da unidade para identificar
     * atividades inseridas, removidas ou alteradas, e as competências afetadas.
     *
     * @param codigo O código do subprocesso em revisão.
     * @return Um {@link ImpactoMapaDto} com o detalhamento dos impactos.
     */
    @GetMapping("/{codigo}/impactos-mapa")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verifica os impactos da revisão no mapa de competências")
    public ImpactoMapaDto verificarImpactos(@PathVariable Long codigo) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        return mapaFacade.verificarImpactos(codigo, usuario);
    }

    /**
     * Obtém a estrutura completa de um mapa associado a um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link MapaCompletoDto} com as competências e atividades do mapa.
     */
    @GetMapping("/{codigo}/mapa")
    @PreAuthorize("isAuthenticated()")
    public MapaCompletoDto obterMapa(@PathVariable Long codigo) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocessoComMapa(codigo);
        return mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codigo);
    }

    /**
     * Obtém uma representação aninhada e formatada do mapa de um subprocesso, ideal para telas de
     * visualização.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Um {@link MapaVisualizacaoDto} com a estrutura hierárquica completa do mapa.
     */
    @GetMapping("/{codigo}/mapa-visualizacao")
    @PreAuthorize("isAuthenticated()")
    public MapaVisualizacaoDto obterMapaVisualizacao(@PathVariable("codigo") Long codSubprocesso) {
        return mapaFacade.obterMapaParaVisualizacao(codSubprocesso);
    }

    /**
     * Lista todas as atividades de um subprocesso com seus conhecimentos.
     *
     * <p>Retorna uma lista plana de atividades (sem agrupamento por competências),
     * ideal para uso em stores e componentes que precisam apenas da lista de atividades.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Uma lista de {@link AtividadeVisualizacaoDto} com as atividades e conhecimentos.
     */
    @GetMapping("/{codSubprocesso}/atividades")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todas as atividades de um subprocesso")
    public ResponseEntity<List<AtividadeVisualizacaoDto>> listarAtividades(
            @PathVariable Long codSubprocesso) {
        List<AtividadeVisualizacaoDto> atividades =
                subprocessoFacade.listarAtividadesSubprocesso(codSubprocesso);
        return ResponseEntity.ok(atividades);
    }

    /**
     * Salva as alterações feitas no mapa de um subprocesso.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as alterações do mapa.
     * @return O {@link MapaCompletoDto} representando o estado atualizado do mapa.
     */
    @PostMapping("/{codigo}/mapa/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MapaCompletoDto salvarMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Object principal) {
        return subprocessoFacade.salvarMapaSubprocesso(
                codigo, request);
    }

    /**
     * Obtém os dados de um mapa para a tela de ajuste pós-validação.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link MapaAjusteDto} com os dados necessários para o ajuste.
     */
    @GetMapping("/{codigo}/mapa-ajuste")
    @PreAuthorize("hasRole('ADMIN')")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long codigo) {
        return subprocessoFacade.obterMapaParaAjuste(codigo);
    }

    /**
     * Salva os ajustes realizados em um mapa após a fase de validação.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as competências ajustadas.
     */
    @PostMapping("/{codigo}/mapa-ajuste/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void salvarAjustesMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarAjustesReq request,
            @AuthenticationPrincipal Object principal) {
        subprocessoFacade.salvarAjustesMapa(
                codigo, request.getCompetencias(), extractTituloUsuario(principal));
    }

    /**
     * Obtém a estrutura completa de um mapa, incluindo competências e as atividades associadas a
     * cada uma.
     *
     * @param codigo código do subprocesso.
     * @return Um {@link ResponseEntity} com o {@link MapaCompletoDto}.
     */
    @GetMapping("/{codigo}/mapa-completo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém um mapa completo com competências e atividades (CDU-15)")
    public ResponseEntity<MapaCompletoDto> obterMapaCompleto(@PathVariable Long codigo) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocessoComMapa(codigo);
        MapaCompletoDto mapa =
                mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codigo);
        return ResponseEntity.ok(mapa);
    }

    /**
     * Salva a estrutura completa de um mapa, incluindo a criação/edição de competências e a
     * atualização de seus vínculos com atividades.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO com a estrutura completa do mapa a ser salvo.
     * @return Um {@link ResponseEntity} com o {@link MapaCompletoDto} atualizado.
     */
    @PostMapping("/{codigo}/mapa-completo/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "Salva um mapa completo com competências e atividades")
    public ResponseEntity<MapaCompletoDto> salvarMapaCompleto(
            @PathVariable Long codigo,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Object principal) {

        MapaCompletoDto mapa = subprocessoFacade.salvarMapaSubprocesso(
                codigo, request);

        return ResponseEntity.ok(mapa);
    }

    /**
     * Disponibiliza mapas de competências de múltiplas unidades em bloco.
     * (CDU-24)
     */
    @PostMapping("/{codigo}/disponibilizar-mapa-bloco")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disponibiliza mapas em bloco")
    public void disponibilizarMapaEmBloco(@PathVariable Long codigo,
                                          @RequestBody @Valid ProcessarEmBlocoRequest request,
                                          @AuthenticationPrincipal Usuario usuario) {
        DisponibilizarMapaRequest serviceRequest = DisponibilizarMapaRequest.builder()
                .dataLimite(request.getDataLimite())
                .build();
        subprocessoFacade.disponibilizarMapaEmBloco(request.getUnidadeCodigos(), codigo, serviceRequest, usuario);
    }

    @PostMapping("/{codigo}/competencias")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "Adiciona uma nova competência a um mapa")
    public ResponseEntity<MapaCompletoDto> adicionarCompetencia(
            @PathVariable Long codigo,
            @RequestBody @Valid CompetenciaReq request,
            @AuthenticationPrincipal Object principal) {
        MapaCompletoDto mapa =
                subprocessoFacade.adicionarCompetencia(
                        codigo, request);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencias/{codCompetencia}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "Atualiza uma competência existente em um mapa")
    public ResponseEntity<MapaCompletoDto> atualizarCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia,
            @RequestBody @Valid CompetenciaReq request,
            @AuthenticationPrincipal Object principal) {

        MapaCompletoDto mapa = subprocessoFacade.atualizarCompetencia(
                codigo, codCompetencia, request);

        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/{codigo}/competencias/{codCompetencia}/remover")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "Remove uma competência de um mapa")
    public ResponseEntity<MapaCompletoDto> removerCompetencia(
            @PathVariable Long codigo,
            @PathVariable Long codCompetencia,
            @AuthenticationPrincipal Object principal) {
        MapaCompletoDto mapa = subprocessoFacade.removerCompetencia(
                codigo, codCompetencia);

        return ResponseEntity.ok(mapa);
    }

    private String extractTituloUsuario(Object principal) {
        return usuarioService.extractTituloUsuario(principal);
    }
}
