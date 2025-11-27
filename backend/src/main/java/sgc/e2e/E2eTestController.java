package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Pageable; // Added for Pageable.unpaged()


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
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource; // This will be the E2eDataSourceRouter
    private final E2eTestDatabaseService e2eTestDatabaseService; // Inject the service
    private final ResourceLoader resourceLoader; // Add this line

    // ... existing methods ...

    /**
     * Recarrega os dados de teste a partir do arquivo SQL (data-minimal.sql ou data.sql).
     * Deleta todos os dados primeiro (desabilitando constraints), depois reinsere os dados de teste iniciais.
     * Útil para resetar o estado do banco entre rodadas de testes.
     */
    @PostMapping("/dados-teste/recarregar")
    public ResponseEntity<Map<String, String>> recarregarDadosTeste() {
        log.info("Iniciando recarga de dados de teste.");
        try {
            log.info("Contagem de entidades ANTES do deleteAll:");
            log.info("  Alerta: {}", alertaRepo.count());
            log.info("  Movimentacao: {}", movimentacaoRepo.count());
            log.info("  Subprocesso: {}", subprocessoRepo.count());
            log.info("  Processo: {}", processoRepo.count());

            // Deleta todos os dados na ordem correta
            clearAllTables();
            log.info("Dados deletados via repositorios.");

            log.info("Contagem de entidades APOS o deleteAll:");
            log.info("  Alerta: {}", alertaRepo.count());
            log.info("  Movimentacao: {}", movimentacaoRepo.count());
            log.info("  Subprocesso: {}", subprocessoRepo.count());
            log.info("  Processo: {}", processoRepo.count());

            // Use E2eTestDatabaseService to reload the database scripts
            try (Connection conn = dataSource.getConnection()) {
                e2eTestDatabaseService.reloadDatabaseScripts(conn, resourceLoader);
            }
            log.info("Execucao do arquivo SQL finalizada.");

            log.info("Contagem de entidades APOS a execucao do SQL:");
            log.info("  Alerta: {}", alertaRepo.count());
            log.info("  Movimentacao: {}", movimentacaoRepo.count());
            log.info("  Subprocesso: {}", subprocessoRepo.count());
            log.info("  Processo: {}", processoRepo.count());

            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "mensagem", "Dados de teste recarregados com sucesso"
            ));
        } catch (Exception e) {
            log.error("Erro critico ao recarregar dados de teste: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "erro",
                    "mensagem", "Erro ao recarregar dados: " + e.getMessage()
            ));
        }
    }

    private void clearAllTables() {
        alertaUsuarioRepo.deleteAll();
        alertaRepo.deleteAll();
        movimentacaoRepo.deleteAll();
        subprocessoRepo.deleteAll();
        processoRepo.deleteAll();
    }


    /**
     * DEBUG: Dados de unidades vinculadas a um processo.
     */
    @GetMapping("/debug/processos/{processoId}/unidades")
    public ResponseEntity<List<Map<String, Object>>> debugUnidadesDoProcesso(@PathVariable Long processoId) {
        return processoRepo.findById(processoId)
                .map(processo -> {
                    var dados = processo.getParticipantes().stream()
                            .map(u -> Map.of(
                                    "codigo", (Object) u.getCodigo(),
                                    "nome", u.getNome(),
                                    "sigla", (Object) u.getSigla()
                            ))
                            .toList();
                    return ResponseEntity.ok(dados);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para criar um banco de dados isolado para um teste específico.
     * Delega para o serviço de banco de dados E2E.
     */
    @PostMapping("/setup/create-isolated-db")
    public ResponseEntity<Map<String, String>> createIsolatedDatabase(@RequestBody Map<String, String> request) {
        String testId = request.get("testId");
        if (testId == null || testId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "erro",
                    "mensagem", "testId é obrigatório"
            ));
        }
        try {
            e2eTestDatabaseService.getOrCreateDataSource(testId);
            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "testId", testId,
                    "mensagem", "Banco de dados isolado criado com sucesso"
            ));
        } catch (Exception e) {
            log.error("Erro ao criar banco isolado: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "erro",
                    "mensagem", "Erro ao criar banco isolado: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para limpar um banco de dados isolado.
     * Delega para o serviço de banco de dados E2E.
     */
    @PostMapping("/setup/cleanup-db/{testId}")
    public ResponseEntity<Map<String, String>> cleanupIsolatedDatabase(@PathVariable String testId) {
        try {
            e2eTestDatabaseService.cleanupDataSource(testId);
            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "mensagem", "Banco de dados isolado removido com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "erro",
                    "mensagem", "Erro ao limpar banco: " + e.getMessage()
            ));
        }
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
            subprocessos.forEach(sp -> {
                movimentacaoRepo.deleteAll(movimentacaoRepo.findBySubprocessoCodigo(sp.getCodigo()));
            });
            subprocessoRepo.deleteAll(subprocessos);
            processoRepo.deleteById(codProcesso);
        });
    }
}
