package sgc.processo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.*;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.dto.SubprocessoListagemDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.LocalizacaoSubprocessoService;
import sgc.subprocesso.service.SubprocessoConsultaService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
@Tag(name = "Processos", description = "Endpoints para gerenciamento de processos de mapeamento, revisão e diagnóstico")
@PreAuthorize("isAuthenticated()")
public class ProcessoController {
    private final ProcessoService processoService;
    private final SubprocessoConsultaService consultaService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final ProcessoDtoMapper processoDtoMapper;
    private final sgc.subprocesso.SubprocessoDtoMapper subprocessoDtoMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessoResumoDto> criar(@Valid @RequestBody CriarProcessoRequest requisicao) {
        Processo criado = processoService.criar(requisicao);
        Long codigo = criado.getCodigo();

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(codigo)
                .toUri();

        return ResponseEntity.created(uri).body(processoDtoMapper.paraResumo(criado));
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    public ResponseEntity<ProcessoResumoDto> obterPorCodigo(@PathVariable Long codigo) {
        return processoService
                .buscarOpt(codigo)
                .map(processoDtoMapper::paraResumo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessoResumoDto> atualizar(
            @PathVariable Long codigo, @Valid @RequestBody AtualizarProcessoRequest requisicao) {
        Processo atualizado = processoService.atualizar(codigo, requisicao);
        return ResponseEntity.ok(processoDtoMapper.paraResumo(atualizado));
    }

    @PostMapping("/{codigo}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        processoService.apagar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/finalizados")
    @Operation(summary = "Lista todos os processos com situação FINALIZADO, aceitando opcionalmente filtro para importação")
    public ResponseEntity<List<ProcessoResumoDto>> listarFinalizados(
            @RequestParam(name = "elegivelImportacao", required = false) Boolean elegivelImportacao) {
        List<Processo> processos = Boolean.TRUE.equals(elegivelImportacao)
                ? processoService.listarParaImportacao()
                : processoService.listarFinalizados();
        return ResponseEntity.ok(processos.stream()
                .map(processoDtoMapper::paraResumo)
                .toList());
    }

    @SuppressWarnings("UnusedReturnValue")
    @GetMapping("/{codigo}/unidades-importacao")
    @PreAuthorize("hasAnyRole('CHEFE', 'ADMIN')")
    @Operation(summary = "Lista todas as unidades participantes de um processo finalizado para importação")
    public ResponseEntity<List<ProcessoDetalheDto.UnidadeParticipanteDto>> listarUnidadesParaImportacao(
            @PathVariable Long codigo) {
        Processo processo = processoService.buscarPorCodigoComParticipantes(codigo);
        if (processo.getSituacao() != SituacaoProcesso.FINALIZADO) {
            throw new ErroValidacao(Mensagens.PROCESSO_DEVE_ESTAR_FINALIZADO);
        }
        Map<Long, Subprocesso> subprocessosPorUnidade = consultaService.listarEntidadesPorProcesso(codigo).stream()
                .collect(Collectors.toMap(sp -> sp.getUnidade().getCodigo(), Function.identity(), (primeiro, duplicado) -> primeiro));
        Map<Long, Unidade> localizacoesPorSubprocesso = localizacaoSubprocessoService.obterLocalizacoesAtuais(subprocessosPorUnidade.values());
        List<ProcessoDetalheDto.UnidadeParticipanteDto> dtos = processo.getParticipantes().stream()
                .map(snapshot -> {
                    ProcessoDetalheDto.UnidadeParticipanteDto dto = processoDtoMapper.paraUnidadeParticipante(snapshot);
                    Subprocesso subprocesso = subprocessosPorUnidade.get(snapshot.getUnidadeCodigoPersistido());
                    if (subprocesso != null) {
                        processoDtoMapper.preencherParticipanteComSubprocesso(
                                dto,
                                subprocesso,
                                java.util.Objects.requireNonNullElseGet(localizacoesPorSubprocesso.get(subprocesso.getCodigo()),
                                        () -> localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso))
                        );
                    }
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ativos")
    @Operation(summary = "Lista todos os processos com situação EM_ANDAMENTO")
    public ResponseEntity<List<ProcessoResumoDto>> listarAtivos() {
        return ResponseEntity.ok(processoService.listarAtivos().stream()
                .map(processoDtoMapper::paraResumo)
                .toList());
    }

    @GetMapping("/{codigo}/detalhes")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    public ResponseEntity<ProcessoDetalheDto> obterDetalhes(@PathVariable Long codigo) {
        ProcessoDetalheDto detalhes = processoService.obterDetalhesCompleto(codigo, false);
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping("/{codigo}/contexto-completo")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
    @Operation(summary = "Obtém o contexto completo para visualização de processo (BFF)")
    public ResponseEntity<ProcessoDetalheDto> obterContextoCompleto(@PathVariable Long codigo) {
        return ResponseEntity.ok(processoService.obterDetalhesCompleto(codigo, true));
    }

    @SuppressWarnings("UnusedReturnValue")
    @PostMapping("/{codigo}/iniciar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inicia um processo")
    public ResponseEntity<ProcessoResumoDto> iniciar(
            @PathVariable Long codigo,
            @Valid @RequestBody IniciarProcessoRequest req) {
        processoService.iniciar(codigo, req.unidades());

        Processo processoAtualizado = processoService.buscarPorCodigoComParticipantes(codigo);
        return ResponseEntity.ok(processoDtoMapper.paraResumo(processoAtualizado));
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
    public ResponseEntity<List<SubprocessoListagemDto>> listarSubprocessos(@PathVariable Long codigo) {
        return ResponseEntity.ok(consultaService.listarEntidadesPorProcesso(codigo).stream()
                .map(subprocessoDtoMapper::paraListagem)
                .toList());
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
        processoService.executarAcaoEmBloco(codigo, request.paraCommand());
        return ResponseEntity.ok().build();
    }
}
