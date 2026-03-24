package sgc.processo;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
@Tag(name = "Processos", description = "Endpoints para gerenciamento de processos de mapeamento, revisão e diagnóstico")
@PreAuthorize("isAuthenticated()")
public class ProcessoController {
    private final ProcessoService processoService;
    private final SubprocessoService subprocessoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> criar(@Valid @RequestBody CriarProcessoRequest requisicao) {
        Processo criado = processoService.criar(requisicao);
        Long codigo = Objects.requireNonNull(criado.getCodigo(), "Código do processo não pode ser nulo");

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(codigo)
                .toUri();

        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/status-unidades")
    @Operation(summary = "Retorna unidades desabilitadas por tipo e processo")
    public ResponseEntity<Map<String, List<Long>>> obterStatusUnidades(
            @RequestParam String tipo, @RequestParam(required = false) Long codProcesso) {
        List<Long> unidadesDesabilitadas = processoService.listarUnidadesBloqueadasPorTipo(TipoProcesso.valueOf(tipo));
        return ResponseEntity.ok(Map.of("unidadesDesabilitadas", unidadesDesabilitadas));
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> obterPorCodigo(@PathVariable Long codigo) {
        return processoService
                .buscarOpt(codigo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody AtualizarProcessoRequest requisicao) {
        Processo atualizado = processoService.atualizar(codigo, requisicao);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{codigo}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        processoService.apagar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/finalizados")
    @Operation(summary = "Lista todos os processos com situação FINALIZADO")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<List<Processo>> listarFinalizados() {
        return ResponseEntity.ok(processoService.listarFinalizados());
    }

    @GetMapping("/para-importacao")
    @Operation(summary = "Lista processos finalizados elegíveis para servirem de base de importação de atividades")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<List<Processo>> listarParaImportacao() {
        return ResponseEntity.ok(processoService.listarParaImportacao());
    }

    @GetMapping("/{codigo}/unidades-importacao")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Lista todas as unidades participantes de um processo finalizado para importação")
    public ResponseEntity<List<ProcessoDetalheDto.UnidadeParticipanteDto>> listarUnidadesParaImportacao(
            @PathVariable Long codigo) {
        Processo processo = processoService.buscarPorCodigoComParticipantes(codigo);
        if (processo.getSituacao() != SituacaoProcesso.FINALIZADO) {
            throw new ErroValidacao(Mensagens.PROCESSO_DEVE_ESTAR_FINALIZADO);
        }
        Map<Long, Subprocesso> subprocessosPorUnidade = subprocessoService.listarEntidadesPorProcesso(codigo).stream()
                .collect(Collectors.toMap(sp -> sp.getUnidade().getCodigo(), Function.identity(), (primeiro, duplicado) -> primeiro));
        List<ProcessoDetalheDto.UnidadeParticipanteDto> dtos = processo.getParticipantes().stream()
                .map(snapshot -> {
                    ProcessoDetalheDto.UnidadeParticipanteDto dto = ProcessoDetalheDto.UnidadeParticipanteDto.fromSnapshot(snapshot);
                    Subprocesso subprocesso = subprocessosPorUnidade.get(snapshot.getUnidadeCodigo());
                    if (subprocesso != null) {
                        dto.setSituacaoSubprocesso(subprocesso.getSituacao());
                        dto.setDataLimite(subprocesso.getDataLimiteEtapa1());
                        dto.setCodSubprocesso(subprocesso.getCodigo());
                        if (subprocesso.getMapa() != null) dto.setMapaCodigo(subprocesso.getMapa().getCodigo());
                        if (subprocesso.getLocalizacaoAtual() != null) {
                            dto.setLocalizacaoAtualCodigo(subprocesso.getLocalizacaoAtual().getCodigo());
                        }
                    }
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ativos")
    @Operation(summary = "Lista todos os processos com situação EM_ANDAMENTO")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<List<Processo>> listarAtivos() {
        return ResponseEntity.ok(processoService.listarAtivos());
    }

    @GetMapping("/{codigo}/detalhes")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    public ResponseEntity<ProcessoDetalheDto> obterDetalhes(
            @PathVariable Long codigo,
            @AuthenticationPrincipal Usuario usuario) {
        ProcessoDetalheDto detalhes = processoService.obterDetalhesCompleto(codigo, usuario, false);
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping("/{codigo}/contexto-completo")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    @Operation(summary = "Obtém o contexto completo para visualização de processo (BFF)")
    public ResponseEntity<ProcessoDetalheDto> obterContextoCompleto(
            @PathVariable Long codigo,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(processoService.obterDetalhesCompleto(codigo, usuario, true));
    }

    @PostMapping("/{codigo}/iniciar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inicia um processo")
    @JsonView(ProcessoViews.Publica.class)
    public ResponseEntity<Processo> iniciar(
            @PathVariable Long codigo,
            @Valid @RequestBody IniciarProcessoRequest req,
            @AuthenticationPrincipal Usuario usuario) {
        List<String> erros = processoService.iniciar(codigo, req.unidades(), usuario);
        if (!erros.isEmpty()) {
            throw new ErroValidacao(String.join(". ", erros));
        }

        Processo processoAtualizado = processoService.buscarPorCodigoComParticipantes(codigo);
        return ResponseEntity.ok(processoAtualizado);
    }

    @PostMapping("/{codigo}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Finaliza um processo (CDU-21)")
    public ResponseEntity<Void> finalizar(@PathVariable Long codigo) {
        processoService.finalizar(codigo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unidades-bloqueadas")
    @Operation(summary = "Lista unidades que já participam de processos ativos por tipo")
    public ResponseEntity<List<Long>> listarUnidadesBloqueadas(@RequestParam String tipo) {
        List<Long> unidadesBloqueadas = processoService.listarUnidadesBloqueadasPorTipo(TipoProcesso.valueOf(tipo));
        return ResponseEntity.ok(unidadesBloqueadas);
    }

    @GetMapping("/{codigo}/subprocessos-elegiveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Lista subprocessos elegíveis para ações em bloco")
    public ResponseEntity<List<SubprocessoElegivelDto>> listarSubprocessosElegiveis(
            @PathVariable Long codigo) {
        List<SubprocessoElegivelDto> elegiveis = processoService.listarSubprocessosElegiveis(codigo);
        return ResponseEntity.ok(elegiveis);
    }

    @GetMapping("/{codigo}/subprocessos")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    @Operation(summary = "Lista todos os subprocessos de um processo")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<List<Subprocesso>> listarSubprocessos(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoService.listarEntidadesPorProcesso(codigo));
    }

    @PostMapping("/{codigo}/enviar-lembrete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Envia lembrete de prazo para unidade")
    public void enviarLembrete(
            @PathVariable Long codigo,
            @RequestBody @Valid EnviarLembreteRequest request) {
        processoService.enviarLembrete(codigo, request.unidadeCodigo());
    }

    @PostMapping("/{codigo}/acao-em-bloco")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Executa ação em bloco para um processo")
    public ResponseEntity<Void> executarAcaoEmBloco(
            @PathVariable Long codigo,
            @Valid @RequestBody AcaoEmBlocoRequest request) {
        processoService.executarAcaoEmBloco(codigo, request);
        return ResponseEntity.ok().build();
    }
}
