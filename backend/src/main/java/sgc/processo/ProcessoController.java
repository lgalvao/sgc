package sgc.processo;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.*;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoViews;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoViews;

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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> criar(@Valid @RequestBody CriarProcessoRequest requisicao) {
        Processo criado = processoFacade.criar(requisicao);
        URI uri = URI.create("/api/processos/%d".formatted(criado.getCodigo()));
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/status-unidades")
    @Operation(summary = "Retorna unidades desabilitadas por tipo e processo")
    public ResponseEntity<Map<String, List<Long>>> obterStatusUnidades(
            @RequestParam String tipo, @RequestParam(required = false) Long codProcesso) {
        List<Long> unidadesDesabilitadas = processoFacade.listarUnidadesBloqueadasPorTipo(tipo);
        return ResponseEntity.ok(Map.of("unidadesDesabilitadas", unidadesDesabilitadas));
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> obterPorId(@PathVariable Long codigo) {
        return processoFacade
                .obterPorId(codigo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody AtualizarProcessoRequest requisicao) {
        Processo atualizado = processoFacade.atualizar(codigo, requisicao);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{codigo}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        processoFacade.apagar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/finalizados")
    @Operation(summary = "Lista todos os processos com situação FINALIZADO")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<List<Processo>> listarFinalizados() {
        return ResponseEntity.ok(processoFacade.listarFinalizados());
    }

    @GetMapping("/ativos")
    @Operation(summary = "Lista todos os processos com situação EM_ANDAMENTO")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<List<Processo>> listarAtivos() {
        return ResponseEntity.ok(processoFacade.listarAtivos());
    }

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

    @PostMapping("/{codigo}/iniciar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inicia um processo")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> iniciar(
            @PathVariable Long codigo, @Valid @RequestBody IniciarProcessoRequest req) {

        var processador = getProcessadoresInicio().get(req.tipo());

        List<String> erros = processador.apply(codigo, req.unidades());
        if (!erros.isEmpty()) {
            throw new ErroProcesso(String.join(". ", erros));
        }

        Processo processoAtualizado = processoFacade.obterEntidadePorId(codigo);
        return ResponseEntity.ok(processoAtualizado);
    }

    @PostMapping("/{codigo}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Finaliza um processo (CDU-21)")
    public ResponseEntity<Void> finalizar(@PathVariable Long codigo) {
        processoFacade.finalizar(codigo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unidades-bloqueadas")
    @Operation(summary = "Lista unidades que já participam de processos ativos por tipo")
    public ResponseEntity<List<Long>> listarUnidadesBloqueadas(@RequestParam String tipo) {
        List<Long> unidadesBloqueadas = processoFacade.listarUnidadesBloqueadasPorTipo(tipo);
        return ResponseEntity.ok(unidadesBloqueadas);
    }

    @GetMapping("/{codigo}/subprocessos-elegiveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Lista subprocessos elegíveis para ações em bloco")
    public ResponseEntity<List<SubprocessoElegivelDto>> listarSubprocessosElegiveis(
            @PathVariable Long codigo) {
        List<SubprocessoElegivelDto> elegiveis = processoFacade.listarSubprocessosElegiveis(codigo);
        return ResponseEntity.ok(elegiveis);
    }

    @GetMapping("/{codigo}/subprocessos")
    @Operation(summary = "Lista todos os subprocessos de um processo")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<List<Subprocesso>> listarSubprocessos(@PathVariable Long codigo) {
        List<Subprocesso> subprocessos = processoFacade.listarEntidadesSubprocessos(codigo);
        return ResponseEntity.ok(subprocessos);
    }

    @PostMapping("/{codigo}/enviar-lembrete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Envia lembrete de prazo para unidade")
    public void enviarLembrete(
            @PathVariable Long codigo,
            @RequestBody @Valid EnviarLembreteRequest request) {
        processoFacade.enviarLembrete(codigo, request.unidadeCodigo());
    }
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
