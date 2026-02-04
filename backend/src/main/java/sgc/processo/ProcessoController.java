package sgc.processo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.*;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.dto.SubprocessoDto;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Controller REST para Processos. Implementa endpoints CRUD e ações de
 * iniciar/finalizar processo
 */
@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
@Tag(name = "Processos", description = "Endpoints para gerenciamento de processos de mapeamento, revisão e diagnóstico")
public class ProcessoController {
    private final ProcessoFacade processoFacade;

    // Strategy Pattern: Map de handlers para inicialização de processo por tipo
    Map<TipoProcesso, BiFunction<Long, List<Long>, List<String>>> getProcessadoresInicio() {
        return Map.of(
                TipoProcesso.MAPEAMENTO, processoFacade::iniciarProcessoMapeamento,
                TipoProcesso.REVISAO, processoFacade::iniciarProcessoRevisao,
                TipoProcesso.DIAGNOSTICO, processoFacade::iniciarProcessoDiagnostico);
    }

    /**
     * Cria um novo processo.
     *
     * @param requisicao O DTO com os dados para a criação do processo.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo
     *         processo e o {@link
     *         ProcessoDto} criado no corpo da resposta.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessoDto> criar(@Valid @RequestBody CriarProcessoRequest requisicao) {
        ProcessoDto criado = processoFacade.criar(requisicao);
        URI uri = URI.create("/api/processos/%d".formatted(criado.getCodigo()));
        return ResponseEntity.created(uri).body(criado);
    }

    /**
     * Retorna códigos de unidades que já participam de processos ativos do tipo
     * especificado ou de um processo específico.
     *
     * @param tipo        Tipo do processo (MAPEAMENTO, REVISAO, DIAGNOSTICO)
     * @param codProcesso Código opcional do processo a ser considerado
     * @return Objeto contendo lista de unidades desabilitadas
     */
    @GetMapping("/status-unidades")
    @Operation(summary = "Retorna unidades desabilitadas por tipo e processo")
    public ResponseEntity<Map<String, List<Long>>> obterStatusUnidades(
            @RequestParam String tipo, @RequestParam(required = false) Long codProcesso) {
        List<Long> unidadesDesabilitadas = processoFacade.listarUnidadesBloqueadasPorTipo(tipo);
        return ResponseEntity.ok(Map.of("unidadesDesabilitadas", unidadesDesabilitadas));
    }

    /**
     * Busca e retorna um processo pelo seu código.
     *
     * @param codigo O código do processo a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link ProcessoDto} ou status
     *         404 Not Found.
     */
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    public ResponseEntity<ProcessoDto> obterPorId(@PathVariable Long codigo) {
        return processoFacade
                .obterPorId(codigo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Atualiza um processo existente.
     *
     * @param codigo     O código do processo a ser atualizado.
     * @param requisicao O DTO com os novos dados do processo.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link ProcessoDto}
     *         atualizado.
     */
    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessoDto> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody AtualizarProcessoRequest requisicao) {
        ProcessoDto atualizado = processoFacade.atualizar(codigo, requisicao);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Exclui um processo.
     *
     * @param codigo O código do processo a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codigo}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        processoFacade.apagar(codigo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos os processos que estão com a situação 'FINALIZADO'.
     *
     * @return Um {@link ResponseEntity} com a lista de {@link ProcessoDto}
     *         finalizados.
     */
    @GetMapping("/finalizados")
    @Operation(summary = "Lista todos os processos com situação FINALIZADO")
    public ResponseEntity<List<ProcessoDto>> listarFinalizados() {
        return ResponseEntity.ok(processoFacade.listarFinalizados());
    }

    /**
     * Lista todos os processos que estão com a situação 'EM_ANDAMENTO'.
     *
     * @return Um {@link ResponseEntity} com a lista de {@link ProcessoDto} ativos.
     */
    @GetMapping("/ativos")
    @Operation(summary = "Lista todos os processos com situação EM_ANDAMENTO")
    public ResponseEntity<List<ProcessoDto>> listarAtivos() {
        return ResponseEntity.ok(processoFacade.listarAtivos());
    }

    /**
     * Retorna os detalhes completos de um processo, incluindo as unidades
     * participantes e o resumo
     * de seus respectivos subprocessos.
     *
     * @param codigo O código do processo a ser detalhado.
     * @param usuario Usuário autenticado (injetado automaticamente).
     * @return Um {@link ResponseEntity} com o {@link ProcessoDetalheDto}.
     */
    @GetMapping("/{codigo}/detalhes")
    @PreAuthorize("hasRole('ADMIN') or @processoFacade.checarAcesso(authentication, #codigo)")
    public ResponseEntity<ProcessoDetalheDto> obterDetalhes(
            @PathVariable Long codigo,
            @AuthenticationPrincipal Usuario usuario) {
        ProcessoDetalheDto detalhes = processoFacade.obterDetalhes(codigo, usuario);
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping("/{codigo}/contexto-completo")
    @PreAuthorize("hasRole('ADMIN') or @processoFacade.checarAcesso(authentication, #codigo)")
    @Operation(summary = "Obtém o contexto completo para visualização de processo (BFF)")
    public ResponseEntity<ProcessoDetalheDto> obterContextoCompleto(
            @PathVariable Long codigo,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(processoFacade.obterContextoCompleto(codigo, usuario));
    }

    /**
     * Inicia um processo, disparando a criação dos subprocessos e notificações.
     *
     * @param codigo O código do processo a ser iniciado.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/{codigo}/iniciar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inicia um processo")
    public ResponseEntity<Object> iniciar(
            @PathVariable Long codigo, @Valid @RequestBody IniciarProcessoRequest req) {

        var processador = getProcessadoresInicio().get(req.tipo());
        if (processador == null) {
            return ResponseEntity.badRequest().build();
        }

        List<String> erros = processador.apply(codigo, req.unidades());
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erros", erros));
        }

        ProcessoDto processoAtualizado = processoFacade
                .obterPorId(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));
        return ResponseEntity.ok(processoAtualizado);
    }

    /**
     * Finaliza um processo, tornando os mapas de seus subprocessos homologados como
     * os novos mapas
     * vigentes para as respectivas unidades.
     *
     * @param codigo O código do processo a ser finalizado.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/{codigo}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Finaliza um processo (CDU-21)")
    public ResponseEntity<Void> finalizar(@PathVariable Long codigo) {
        processoFacade.finalizar(codigo);
        return ResponseEntity.ok().build();
    }

    /**
     * Retorna códigos de unidades que já participam de processos ativos do tipo
     * especificado. Útil
     * para desabilitar checkboxes no frontend durante criação/edição de processos.
     *
     * @param tipo Tipo do processo (MAPEAMENTO, REVISAO, DIAGNOSTICO)
     * @return Lista de códigos de unidades bloqueadas
     */
    @GetMapping("/unidades-bloqueadas")
    @Operation(summary = "Lista unidades que já participam de processos ativos por tipo")
    public ResponseEntity<List<Long>> listarUnidadesBloqueadas(@RequestParam String tipo) {
        List<Long> unidadesBloqueadas = processoFacade.listarUnidadesBloqueadasPorTipo(tipo);
        return ResponseEntity.ok(unidadesBloqueadas);
    }

    /**
     * Retorna uma lista de subprocessos elegíveis para ações em bloco
     * (aceite/homologação) para o
     * usuário autenticado.
     *
     * @param codigo O código do processo.
     * @return Lista de DTOs representando os subprocessos elegíveis.
     */
    @GetMapping("/{codigo}/subprocessos-elegiveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Lista subprocessos elegíveis para ações em bloco")
    public ResponseEntity<List<SubprocessoElegivelDto>> listarSubprocessosElegiveis(
            @PathVariable Long codigo) {
        List<SubprocessoElegivelDto> elegiveis = processoFacade.listarSubprocessosElegiveis(codigo);
        return ResponseEntity.ok(elegiveis);
    }

    /**
     * Retorna todos os subprocessos de um processo. Utilizado pelo frontend para
     * exibir a árvore de
     * unidades e seus subprocessos.
     *
     * @param codigo O código do processo.
     * @return Lista de subprocessos do processo.
     */
    @GetMapping("/{codigo}/subprocessos")
    @Operation(summary = "Lista todos os subprocessos de um processo")
    public ResponseEntity<List<SubprocessoDto>> listarSubprocessos(@PathVariable Long codigo) {
        List<SubprocessoDto> subprocessos = processoFacade.listarTodosSubprocessos(codigo);
        return ResponseEntity.ok(subprocessos);
    }

    /**
     * Envia um lembrete de prazo para uma unidade.
     */
    @PostMapping("/{codigo}/enviar-lembrete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Envia lembrete de prazo para unidade")
    public void enviarLembrete(
            @PathVariable Long codigo,
            @RequestBody @Valid EnviarLembreteRequest request) {
        processoFacade.enviarLembrete(codigo, request.unidadeCodigo());
    }
    /**
     * Executa uma ação em bloco (aceitar, homologar, disponibilizar) para múltiplas unidades
     * de um processo.
     *
     * @param codigo O código do processo.
     * @param request O DTO com a lista de unidades e a ação.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/{codigo}/acao-em-bloco")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Executa ação em bloco para um processo")
    public ResponseEntity<Void> executarAcaoEmBloco(
            @PathVariable Long codigo,
            @Valid @RequestBody AcaoEmBlocoRequest request) {
        processoFacade.executarAcaoEmBloco(codigo, request);
        return ResponseEntity.ok().build();
    }
}
