package sgc.e2e;

import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Endpoints para limpeza e reset de dados em testes E2E.
 *
 * ⚠️ Ativo APENAS no perfil 'e2e'. Operações destrutivas para testes idempotentes.
 */
@RestController
@RequestMapping("/api/e2e")
@RequiredArgsConstructor
@Profile("e2e")
public class E2eTestController {
    private final ProcessoRepo processoRepo;
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource; // This will be the E2eDataSourceRouter
    private final E2eTestDatabaseService e2eTestDatabaseService; // Inject the service

    /**
     * Deleta um processo e suas dependências (alertas, subprocessos, movimentações).
     */
    @PostMapping("/processos/{codigo}/apagar")
    @Transactional
    public ResponseEntity<Void> apagarProcessoPorCodigo(@PathVariable Long codigo) {
        apagarProcessosComDependencias(List.of(codigo));
        return ResponseEntity.noContent().build();
    }

    /**
     * Deleta todos os processos de uma unidade (incluindo os em andamento).
     */
    @PostMapping("/processos/unidade/{codigoUnidade}/limpar")
    @Transactional
    public ResponseEntity<Void> limparProcessosPorUnidade(@PathVariable Long codigoUnidade) {
        var processos = processoRepo.findDistinctByParticipantes_CodigoIn(List.of(codigoUnidade), org.springframework.data.domain.Pageable.unpaged());
        var codigos = processos.stream()
                .map(Processo::getCodigo)
                .distinct()
                .toList();

        if (!codigos.isEmpty()) {
            apagarProcessosComDependencias(codigos);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Deleta todos os processos em andamento.
     */
    @PostMapping("/processos/em-andamento/limpar")
    @Transactional
    public ResponseEntity<Void> limparProcessosEmAndamento() {
        var codigos = processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                .map(Processo::getCodigo)
                .toList();

        if (!codigos.isEmpty()) {
            apagarProcessosComDependencias(codigos);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Deleta todos os processos em andamento E todos os processos das unidades especificadas.
     * Útil para limpar estado entre testes e2e.
     */
    @PostMapping("/processos/unidades-e-pendentes/limpar")
    @Transactional
    public ResponseEntity<Void> limparProcessosPorUnidadesEPendentes(@RequestBody List<Long> codigosUnidades) {
        // Primeiro, limpa todos os processos em andamento (de qualquer unidade)
        var processosEmAndamento = processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                .map(Processo::getCodigo)
                .toList();

        // Depois, limpa todos os processos das unidades específicas
        var processos = processoRepo.findDistinctByParticipantes_CodigoIn(codigosUnidades, org.springframework.data.domain.Pageable.unpaged());
        var processosUnidades = processos.stream()
                .map(Processo::getCodigo)
                .distinct()
                .toList();

        // Combina os dois conjuntos
        var todosCodigos = new java.util.HashSet<Long>();
        todosCodigos.addAll(processosEmAndamento);
        todosCodigos.addAll(processosUnidades);

        if (!todosCodigos.isEmpty()) {
            apagarProcessosComDependencias(new java.util.ArrayList<>(todosCodigos));
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Deleta todo o conteúdo do banco de dados (reset completo).
     */
    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<Void> reset() {
        alertaUsuarioRepo.deleteAll();
        alertaRepo.deleteAll();
        movimentacaoRepo.deleteAll();
        subprocessoRepo.deleteAll();
        processoRepo.deleteAll();

        return ResponseEntity.noContent().build();
    }

    /**
     * Recarrega os dados de teste a partir do arquivo SQL (data-h2-minimal.sql ou data-h2.sql).
     * Deleta todos os dados primeiro (desabilitando constraints), depois reinsere os dados de teste iniciais.
     * Útil para resetar o estado do banco entre rodadas de testes.
     */
    @PostMapping("/dados-teste/recarregar")
    @Transactional
    public ResponseEntity<Map<String, String>> recarregarDadosTeste() {
        try {
            // 1. Desabilita constraints de integridade referencial (H2)
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // 2. Deleta todos os dados na ordem correta
            alertaUsuarioRepo.deleteAll();
            alertaRepo.deleteAll();
            movimentacaoRepo.deleteAll();
            subprocessoRepo.deleteAll();
            processoRepo.deleteAll();

            // 3. Habilita constraints novamente
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

            // 4. Executa o arquivo de dados SQL (preferindo minimal se existir)
            String sqlFilePath = "/data-h2-minimal.sql";
            String sql;
            try {
                sql = new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream(sqlFilePath)))
                        .lines()
                        .collect(Collectors.joining("\n"));
            } catch (Exception e) {
                // Fallback para data-h2.sql completo se minimal não existir
                sqlFilePath = "/data-h2.sql";
                sql = new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream(sqlFilePath)))
                        .lines()
                        .collect(Collectors.joining("\n"));
            }

            // 5. Executa cada statement SQL
            // Use the current DataSource to execute the statements
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        try {
                            stmt.execute(trimmed + ";");
                        } catch (Exception e) {
                            // Ignora erros em statements individuais (ex: constraints, duplicates)
                        }
                    }
                }
            }


            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "arquivo", sqlFilePath,
                    "mensagem", "Dados de teste recarregados com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "erro",
                    "mensagem", "Erro ao recarregar dados: " + e.getMessage()
            ));
        }
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
