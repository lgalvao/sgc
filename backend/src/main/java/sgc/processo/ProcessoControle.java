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
public class ProcessoControle {
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
     * Busca e retorna um processo pelo seu ID.
     *
     * @param id O ID do processo a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link ProcessoDto} ou status 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProcessoDto> obterPorId(@PathVariable Long id) {
        return processoService.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Atualiza um processo existente.
     *
     * @param codProcesso O ID do processo a ser atualizado.
     * @param requisicao O DTO com os novos dados do processo.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link ProcessoDto} atualizado.
     */
    @PostMapping("/{codProcesso}/atualizar")
    public ResponseEntity<ProcessoDto> atualizar(@PathVariable Long codProcesso, @Valid @RequestBody AtualizarProcessoReq requisicao) {
        ProcessoDto atualizado = processoService.atualizar(codProcesso, requisicao);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Exclui um processo.
     *
     * @param codProcesso O ID do processo a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codProcesso}/excluir")
    public ResponseEntity<Void> excluir(@PathVariable Long codProcesso) {
        processoService.apagar(codProcesso);
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
     * Retorna os detalhes completos de um processo, incluindo as unidades participantes
     * e o resumo de seus respectivos subprocessos.
     *
     * @param id O ID do processo a ser detalhado.
     * @return Um {@link ResponseEntity} com o {@link ProcessoDetalheDto}.
     */
    @GetMapping("/{id}/detalhes")
    public ResponseEntity<ProcessoDetalheDto> obterDetalhes(@PathVariable Long id) {
        ProcessoDetalheDto detalhes = processoService.obterDetalhes(id);
        return ResponseEntity.ok(detalhes);
    }

    /**
     * Inicia um processo, disparando a criação dos subprocessos e notificações.
     * <p>
     * Corresponde ao CDU-03. O comportamento varia com base no tipo de processo:
     * 'MAPEAMENTO' ou 'REVISAO'.
     *
     * @param id       O ID do processo a ser iniciado.
     * @param tipo     O tipo de processo ('MAPEAMENTO' ou 'REVISAO').
     * @param unidades Uma lista opcional de IDs de unidades para restringir o início
     *                 do processo a um subconjunto dos participantes.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/{id}/iniciar")
    @Operation(summary = "Inicia um processo (CDU-03)")
    public ResponseEntity<ProcessoDto> iniciar(
            @PathVariable Long id,
            @RequestParam(name = "tipo", required = false, defaultValue = "MAPEAMENTO") String tipo,
            @RequestBody(required = false) List<Long> unidades) {

        if ("REVISAO".equalsIgnoreCase(tipo)) {
            processoService.iniciarProcessoRevisao(id, unidades);
        } else {
            // por padrão, inicia mapeamento
            processoService.iniciarProcessoMapeamento(id, unidades);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Finaliza um processo, tornando os mapas de seus subprocessos homologados
     * como os novos mapas vigentes para as respectivas unidades.
     * <p>
     * Corresponde ao CDU-21.
     *
     * @param id O ID do processo a ser finalizado.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finaliza um processo (CDU-21)")
    public ResponseEntity<?> finalizar(@PathVariable Long id) {
        processoService.finalizar(id);
        return ResponseEntity.ok().build();
    }
}