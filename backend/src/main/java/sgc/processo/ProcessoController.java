package sgc.processo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.service.ProcessoService;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para Processos.
 * Implementa endpoints CRUD e ações de iniciar/finalizar processo conforme CDU-03.
 */
@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
@Tag(name = "Processos", description = "Endpoints para gerenciamento de processos de mapeamento, revisão e diagnóstico")
public class ProcessoController {
    private final ProcessoService processoService;

    /**
     * Cria um novo processo.
     *
     * @param requisicao O DTO com os dados para a criação do processo.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo
     *         processo e o {@link ProcessoDto} criado no corpo da resposta.
     */
    @PostMapping
    public ResponseEntity<ProcessoDto> criar(@Valid @RequestBody CriarProcessoReq requisicao) {
        ProcessoDto criado = processoService.criar(requisicao);
        URI uri = URI.create("/api/processos/%d".formatted(criado.getCodigo()));
        return ResponseEntity.created(uri).body(criado);
    }

    /**
     * Busca e retorna um processo pelo seu código.
     *
     * @param codigo O código do processo a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link ProcessoDto} ou status 404 Not Found.
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<ProcessoDto> obterPorId(@PathVariable Long codigo) {
        return processoService.obterPorId(codigo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Atualiza um processo existente.
     *
     * @param codigo O código do processo a ser atualizado.
     * @param requisicao O DTO com os novos dados do processo.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link ProcessoDto} atualizado.
     */
    @PostMapping("/{codigo}/atualizar")
    public ResponseEntity<ProcessoDto> atualizar(@PathVariable Long codigo, @Valid @RequestBody AtualizarProcessoReq requisicao) {
        ProcessoDto atualizado = processoService.atualizar(codigo, requisicao);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Exclui um processo.
     *
     * @param codigo O código do processo a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codigo}/excluir")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        processoService.apagar(codigo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos os processos que estão com a situação 'FINALIZADO'.
     *
     * @return Um {@link ResponseEntity} com a lista de {@link ProcessoDto} finalizados.
     */
    @GetMapping("/finalizados")
    @Operation(summary = "Lista todos os processos com situação FINALIZADO")
    public ResponseEntity<List<ProcessoDto>> listarFinalizados() {
        return ResponseEntity.ok(processoService.listarFinalizados());
    }

    /**
     * Lista todos os processos que estão com a situação 'EM_ANDAMENTO'.
     *
     * @return Um {@link ResponseEntity} com a lista de {@link ProcessoDto} ativos.
     */
    @GetMapping("/ativos")
    @Operation(summary = "Lista todos os processos com situação EM_ANDAMENTO")
    public ResponseEntity<List<ProcessoDto>> listarAtivos() {
        return ResponseEntity.ok(processoService.listarAtivos());
    }

    /**
     * Retorna os detalhes completos de um processo, incluindo as unidades participantes
     * e o resumo de seus respectivos subprocessos.
     *
     * @param codigo O código do processo a ser detalhado.
     * @return Um {@link ResponseEntity} com o {@link ProcessoDetalheDto}.
     */
    @GetMapping("/{codigo}/detalhes")
    public ResponseEntity<ProcessoDetalheDto> obterDetalhes(@PathVariable Long codigo) {
        ProcessoDetalheDto detalhes = processoService.obterDetalhes(codigo);
        return ResponseEntity.ok(detalhes);
    }

    /**
     * Inicia um processo, disparando a criação dos subprocessos e notificações.
     * <p>
     * Corresponde ao CDU-03. O comportamento varia com base no tipo de processo:
     * 'MAPEAMENTO' ou 'REVISAO'.
     *
     * @param codigo       O código do processo a ser iniciado.
     * @param tipo     O tipo de processo ('MAPEAMENTO' ou 'REVISAO').
     * @param unidades Uma lista opcional de IDs de unidades para restringir o início
     *                 do processo a um subconjunto dos participantes.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/{codigo}/iniciar")
    @Operation(summary = "Inicia um processo (CDU-03)")
    public ResponseEntity<ProcessoDto> iniciar(
            @PathVariable Long codigo,
            @RequestParam(name = "tipo") sgc.processo.modelo.TipoProcesso tipo,
            @RequestBody(required = false) List<Long> unidades) {

        if (tipo == sgc.processo.modelo.TipoProcesso.REVISAO) {
            processoService.iniciarProcessoRevisao(codigo, unidades);
        } else if (tipo == sgc.processo.modelo.TipoProcesso.MAPEAMENTO) {
            processoService.iniciarProcessoMapeamento(codigo, unidades);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Finaliza um processo, tornando os mapas de seus subprocessos homologados
     * como os novos mapas vigentes para as respectivas unidades.
     * <p>
     * Corresponde ao CDU-21.
     *
     * @param codigo O código do processo a ser finalizado.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/{codigo}/finalizar")
    @Operation(summary = "Finaliza um processo (CDU-21)")
    public ResponseEntity<?> finalizar(@PathVariable Long codigo) {
        processoService.finalizar(codigo);
        return ResponseEntity.ok().build();
    }

    /**
     * Retorna códigos de unidades que já participam de processos ativos do tipo especificado.
     * Útil para desabilitar checkboxes no frontend durante criação/edição de processos.
     *
     * @param tipo Tipo do processo (MAPEAMENTO, REVISAO, DIAGNOSTICO)
     * @return Lista de códigos de unidades bloqueadas
     */
    @GetMapping("/unidades-bloqueadas")
    @Operation(summary = "Lista unidades que já participam de processos ativos por tipo")
    public ResponseEntity<List<Long>> listarUnidadesBloqueadas(@RequestParam String tipo) {
        List<Long> unidadesBloqueadas = processoService.listarUnidadesBloqueadasPorTipo(tipo);
        return ResponseEntity.ok(unidadesBloqueadas);
    }
}