package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/e2e")
@RequiredArgsConstructor
@Slf4j
public class E2eTestController {
    private final ProcessoRepo processoRepo;
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final DataSource dataSource;
    private final E2eTestDatabaseService e2eTestDatabaseService;
    private final ResourceLoader resourceLoader;

    @PostMapping("/dados-teste/recarregar")
    public ResponseEntity<Map<String, String>> recarregarDadosTeste() {
        log.info("Recaregando dados de teste.");
        try {
            clearAllTables();
            try (Connection conn = dataSource.getConnection()) {
                e2eTestDatabaseService.reloadDatabaseScripts(conn, resourceLoader);
            }
            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "mensagem", "Dados de teste recarregados com sucesso"));
        } catch (Exception e) {
            log.error("Erro critico ao recarregar dados de teste: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "erro",
                    "mensagem", "Erro ao recarregar dados: " + e.getMessage()));
        }
    }

    private void clearAllTables() {
        alertaUsuarioRepo.deleteAll();
        alertaRepo.deleteAll();
        movimentacaoRepo.deleteAll();
        subprocessoRepo.deleteAll();
        processoRepo.deleteAll();
    }

    @GetMapping("/debug/processos/{processoId}/unidades")
    public ResponseEntity<List<Map<String, Object>>> debugUnidadesDoProcesso(
            @PathVariable @org.jspecify.annotations.NonNull Long processoId) {
        return processoRepo.findById(processoId)
                .map(processo -> {
                    var dados = processo.getParticipantes().stream()
                            .map(u -> Map.of(
                                    "codigo", (Object) u.getCodigo(),
                                    "nome", u.getNome(),
                                    "sigla", (Object) u.getSigla()))
                            .toList();
                    return ResponseEntity.ok(dados);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/processos/{codigo}/apagar")
    @Transactional
    public ResponseEntity<Void> apagarProcesso(@PathVariable Long codigo) {
        if (!processoRepo.existsById(codigo)) {
            return ResponseEntity.noContent().build();
        }
        apagarProcessosComDependencias(List.of(codigo));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/processos/unidade/{unidadeCodigo}/limpar")
    @Transactional
    public ResponseEntity<Void> limparProcessosPorUnidade(@PathVariable Long unidadeCodigo) {
        var processos = processoRepo.findDistinctByParticipantes_CodigoIn(List.of(unidadeCodigo), Pageable.unpaged());
        if (processos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        apagarProcessosComDependencias(processos.stream().map(Processo::getCodigo).toList());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/processos/em-andamento/limpar")
    @Transactional
    public ResponseEntity<Void> limparProcessosEmAndamento() {
        var processos = processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
        if (processos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        apagarProcessosComDependencias(processos.stream().map(Processo::getCodigo).toList());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<Void> resetCompleto() {
        clearAllTables();
        return ResponseEntity.noContent().build();
    }

    private void apagarProcessosComDependencias(List<Long> codigos) {
        var alertaIds = alertaRepo.findIdsByProcessoCodigoIn(codigos);
        if (!alertaIds.isEmpty()) {
            alertaUsuarioRepo.deleteByIdAlertaCodigoIn(alertaIds);
            alertaRepo.deleteByProcessoCodigoIn(codigos);
        }

        codigos.forEach(codProcesso -> {
            var subprocessos = subprocessoRepo.findByProcessoCodigo(codProcesso);
            subprocessos.forEach(
                    sp -> movimentacaoRepo.deleteAll(movimentacaoRepo.findBySubprocessoCodigo(sp.getCodigo())));
            subprocessoRepo.deleteAll(subprocessos);
            processoRepo.deleteById(codProcesso);
        });
    }
}
