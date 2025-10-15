package sgc.processo;

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
import java.util.Map;

/**
 * Controller REST para Processos.
 * Implementa endpoints CRUD e ações de iniciar/finalizar processo conforme CDU-03.
 */
@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
public class ProcessoControle {
    private final ProcessoService processoService;
    private final ProcessoIniciacaoService processoIniciacaoService;
    private final ProcessoFinalizacaoService processoFinalizacaoService;

    @PostMapping
    public ResponseEntity<ProcessoDto> criar(@Valid @RequestBody CriarProcessoReq requisicao) {
        ProcessoDto criado = processoService.criar(requisicao);
        URI uri = URI.create("/api/processos/%d".formatted(criado.getCodigo()));
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessoDto> obterPorId(@PathVariable Long id) {
        return processoService.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessoDto> atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarProcessoReq requisicao) {
        ProcessoDto atualizado = processoService.atualizar(id, requisicao);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        processoService.apagar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna os detalhes completos de um processo, incluindo unidades snapshot e resumo de subprocessos.
     * Este endpoint delega para ProcessoService.obterDetalhes e aplica tratamento de autorização.
     * <p>
     * Exemplo: GET /api/processos/1/detalhes?perfil=ADMIN
     */
    @GetMapping("/{id}/detalhes")
    public ResponseEntity<ProcessoDetalheDto> obterDetalhes(@PathVariable Long id) {
        ProcessoDetalheDto detalhes = processoService.obterDetalhes(id);
        return ResponseEntity.ok(detalhes);
    }

    /**
     * Inicia um processo. O parâmetro `tipo` indica fluxo esperado (MAPEAMENTO, REVISAO, DIAGNOSTICO).
     * O corpo opcional pode conter uma lista de unidades (IDs) que participam do início.
     */
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<ProcessoDto> iniciar(
            @PathVariable Long id,
            @RequestParam(name = "tipo", required = false, defaultValue = "MAPEAMENTO") String tipo,
            @RequestBody(required = false) List<Long> unidades) {

        if ("REVISAO".equalsIgnoreCase(tipo)) {
            processoIniciacaoService.iniciarProcessoRevisao(id, unidades);
        } else {
            // por padrão, inicia mapeamento
            processoIniciacaoService.iniciarProcessoMapeamento(id, unidades);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * CDU-21 - Finalizar processo
     * POST /api/processos/{id}/finalizar
     * <p> <p>
     * Finaliza um processo de mapeamento ou revisão, tornando os mapas vigentes
     * e notificando todas as unidades participantes.
     *
     * @param id ID do processo a ser finalizado
     * @return ProcessoDto com dados do processo finalizado
     */
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable Long id) {
        try {
            processoFinalizacaoService.finalizar(id);
            return ResponseEntity.ok().build();
        } catch (sgc.processo.modelo.ErroProcesso e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("message", e.getMessage()));
        }
    }
}