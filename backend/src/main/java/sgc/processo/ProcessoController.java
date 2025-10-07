package sgc.processo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.processo.dto.ProcessoDTO;
import sgc.processo.dto.ProcessoDetalheDTO;
import sgc.processo.dto.ReqAtualizarProcesso;
import sgc.processo.dto.ReqCriarProcesso;
import sgc.comum.erros.ErroDominioAccessoNegado;

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
public class ProcessoController {
    private final ProcessoService processoService;

    @PostMapping
    public ResponseEntity<ProcessoDTO> criarProcesso(@Valid @RequestBody ReqCriarProcesso requisicao) {
        ProcessoDTO criado = processoService.criar(requisicao);
        URI uri = URI.create("/api/processos/%d".formatted(criado.getCodigo()));
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessoDTO> obterPorId(@PathVariable Long id) {
        return processoService.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessoDTO> atualizarProcesso(@PathVariable Long id, @Valid @RequestBody ReqAtualizarProcesso requisicao) {
        try {
            ProcessoDTO atualizado = processoService.atualizar(id, requisicao);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirProcesso(@PathVariable Long id) {
        try {
            processoService.apagar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retorna os detalhes completos de um processo, incluindo unidades snapshot e resumo de subprocessos.
     * Este endpoint delega para ProcessoService.obterDetalhes e aplica tratamento de autorização.
     * <p>
     * Exemplo: GET /api/processos/1/detalhes?perfil=ADMIN
     */
    @GetMapping("/{id}/detalhes")
    public ResponseEntity<ProcessoDetalheDTO> detalhesProcesso(
            @PathVariable Long id,
            @RequestParam(name = "perfil") String perfil,
            @RequestParam(name = "unidade", required = false) Long unidade) {
        try {
            ProcessoDetalheDTO detalhes = processoService.obterDetalhes(id, perfil, unidade);
            return ResponseEntity.ok(detalhes);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (ErroDominioAccessoNegado ex) {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * Inicia um processo. O parâmetro `tipo` indica fluxo esperado (MAPEAMENTO, REVISAO, DIAGNOSTICO).
     * O corpo opcional pode conter uma lista de unidades (IDs) que participam do início.
     */
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<ProcessoDTO> iniciarProcesso(
            @PathVariable Long id,
            @RequestParam(name = "tipo", required = false, defaultValue = "MAPEAMENTO") String tipo,
            @RequestBody(required = false) List<Long> unidades) {

        try {
            ProcessoDTO resultado;
            if ("REVISAO".equalsIgnoreCase(tipo)) {
                resultado = processoService.iniciarProcessoRevisao(id, unidades);
            } else {
                // por padrão, inicia mapeamento
                resultado = processoService.iniciarProcessoMapeamento(id, unidades);
            }
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * CDU-21 - Finalizar processo
     * POST /api/processos/{id}/finalizar
     * <p> <p>
     * Finaliza um processo de mapeamento ou revisão, tornando os mapas vigentes
     * e notificando todas as unidades participantes.
     *
     * @param id ID do processo a ser finalizado
     * @return ProcessoDTO com dados do processo finalizado
     */
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarProcesso(@PathVariable Long id) {
        try {
            ProcessoDTO finalizado = processoService.finalizarProcesso(id);
            return ResponseEntity.ok(finalizado);
        } catch (IllegalArgumentException ex) {
            // Processo não encontrado
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            // Processo em situação inválida para finalizar
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", ex.getMessage()));
        } catch (ErroProcesso ex) {
            // Validação de negócio falhou (ex: subprocessos não homologados)
            return ResponseEntity.status(422) // Entidade não processável
                    .body(Map.of("erro", ex.getMessage()));
        }
    }
}