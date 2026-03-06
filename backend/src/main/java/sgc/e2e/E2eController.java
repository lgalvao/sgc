package sgc.e2e;

import com.fasterxml.jackson.annotation.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.init.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;

import javax.sql.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("JvmTaintAnalysis")
@RestController
@RequestMapping("/e2e")
@Profile("e2e")
@Slf4j
public class E2eController {
    private static final String SQL_SUBPROCESSO_POR_PROCESSO = " sgc.subprocesso WHERE processo_codigo = ?)";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ProcessoFacade processoFacade;
    private final UnidadeService unidadeService;
    private final ResourceLoader resourceLoader;

    public E2eController(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate,
                         ProcessoFacade processoFacade, UnidadeService unidadeService,
                         ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.processoFacade = processoFacade;
        this.unidadeService = unidadeService;
        this.resourceLoader = resourceLoader;
    }

    @PostMapping("/reset-database")
    public void resetDatabase() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection()) {
            executeDatabaseReset(conn);
            log.info("Reset do banco de dados concluído.");
        } catch (Exception e) {
            log.error("Erro crítico ao resetar banco de dados", e);
            throw new ErroConfiguracao("Falha no reset do banco: %s".formatted(e.getMessage()));
        }
    }

    private void executeDatabaseReset(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            log.debug("Desabilitando integridade referencial");
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_SCHEMA) = 'SGC'",
                    String.class).stream().flatMap(Stream::ofNullable).toList();

            log.debug("Limpando {} tabelas no schema SGC", tables.size());
            for (String table : tables) {
                limparTabela(stmt, table);
            }

            Resource seedResource = getSeedResource();
            log.debug("Executando script de seed: {}", seedResource.getFilename());
            ScriptUtils.executeSqlScript(conn, seedResource);

            log.debug("Reabilitando integridade referencial");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    private void limparTabela(Statement stmt, String table) throws SQLException {
        log.debug("Limpando tabela: sgc.{}", table);
        try {
            stmt.execute("TRUNCATE TABLE sgc." + table + " RESTART IDENTITY");
        } catch (Exception e) {
            log.warn("Não foi possível truncar tabela {}, tentando DELETE: {}", table, e.getMessage());
            stmt.execute("DELETE FROM sgc." + table);
        }
    }

    private Resource getSeedResource() {
        Resource seedResource = resourceLoader.getResource("file:../e2e/setup/seed.sql");
        if (!seedResource.exists()) {
            seedResource = resourceLoader.getResource("file:e2e/setup/seed.sql");
        }
        if (!seedResource.exists()) {
            throw new ErroConfiguracao("Arquivo seed.sql não encontrado");
        }
        return seedResource;
    }

    @PostMapping("/processo/{codigo}/limpar-completo")
    @Transactional
    public void limparProcessoCompleto(@PathVariable Long codigo) {
        log.info("Limpando processo {} (modo robusto)", codigo);
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            String subquerySubprocessos = "(SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = " + codigo + ")";
            String subqueryMapas = "(SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN " + subquerySubprocessos + ")";

            jdbcTemplate.update("DELETE FROM sgc.alerta_usuario WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)", codigo);
            jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);

            jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT codigo FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas);
            jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas);

            // Limpar referências de Mapa Vigente antes de excluir o Mapa
            jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE mapa_vigente_codigo IN " + subqueryMapas);
            jdbcTemplate.update("DELETE FROM sgc.mapa WHERE subprocesso_codigo IN " + subquerySubprocessos);

            jdbcTemplate.update("DELETE FROM sgc.analise WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);

            jdbcTemplate.update("DELETE FROM sgc.unidade_processo WHERE processo_codigo = ?", codigo);
            jdbcTemplate.update("DELETE FROM sgc.processo WHERE codigo = ?", codigo);

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            log.info("Limpeza robusta do processo {} concluída.", codigo);
        } catch (SQLException e) {
            log.error("Erro na limpeza robusta do processo {}", codigo, e);
            throw new RuntimeException("Falha na limpeza do processo: " + e.getMessage());
        }
    }

    @PostMapping("/processo/{codigo}/limpar")
    @Transactional
    public void limparProcessoComDependentes(@PathVariable Long codigo) {
        String sqlMapas =
                "SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN (SELECT codigo FROM" + SQL_SUBPROCESSO_POR_PROCESSO;
        List<Long> mapaIds = jdbcTemplate.queryForList(sqlMapas, Long.class, codigo).stream()
                .flatMap(Stream::ofNullable)
                .toList();

        jdbcTemplate.update(
                "DELETE FROM sgc.analise WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + SQL_SUBPROCESSO_POR_PROCESSO,
                codigo);
        jdbcTemplate.update(
                "DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + SQL_SUBPROCESSO_POR_PROCESSO,
                codigo);
        jdbcTemplate.update(
                "DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + SQL_SUBPROCESSO_POR_PROCESSO,
                codigo);


        if (!mapaIds.isEmpty()) {
            Map<String, Object> params = Map.of("ids", mapaIds);

            namedJdbcTemplate.update(
                    "DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM"
                            + " sgc.atividade WHERE mapa_codigo IN (:ids))", params);
            namedJdbcTemplate.update(
                    "DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo"
                            + " FROM sgc.atividade WHERE mapa_codigo IN (:ids))", params);
            namedJdbcTemplate.update(
                    "DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT"
                            + " codigo FROM sgc.competencia WHERE mapa_codigo IN (:ids))", params);
            namedJdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN (:ids)", params);
            namedJdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN (:ids)", params);
            namedJdbcTemplate.update("DELETE FROM sgc.mapa WHERE codigo IN (:ids)", params);
        }
        jdbcTemplate.update("""
                DELETE FROM sgc.alerta_usuario
                WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)""", codigo);
        jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.unidade_processo WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.processo WHERE codigo = ?", codigo);
    }

    /**
     * Cria um processo de mapeamento via API para testes E2E. Mais rápido que criar via UI.
     */
    @PostMapping("/fixtures/processo-mapeamento")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamento(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.MAPEAMENTO));
    }

    /**
     * Cria um processo de revisão via API para testes E2E. Mais rápido que criar via UI.
     */
    @PostMapping("/fixtures/processo-revisao")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisao(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.REVISAO));
    }

    /**
     * Cria um processo já finalizado e insere atividades/mapa forçadamente via SQL
     * para pular o workflow e testar a importação de forma ultra-rápida.
     */
    @PostMapping("/fixtures/processo-finalizado-com-atividades")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoFinalizadoComAtividades(@RequestBody ProcessoFixtureRequest request) {
        Processo processo = executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.MAPEAMENTO));
        Long procId = processo.getCodigo();
        
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Long unidId = unidade.getCodigo();

        // Subprocesso
        Long subId = jdbcTemplate.queryForObject("SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ? AND unidade_codigo = ?", Long.class, procId, unidId);
        
        // Mapa
        Long mapaId = jdbcTemplate.queryForObject("SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo = ?", Long.class, subId);
        
        // Atividades com conhecimentos associados
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)", mapaId, "Atividade Origem A - " + procId);
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)", mapaId, "Atividade Origem B - " + procId);

        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) SELECT codigo, ? FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                "Conhecimento A - " + procId, mapaId, "Atividade Origem A - " + procId);
        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) SELECT codigo, ? FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                "Conhecimento B - " + procId, mapaId, "Atividade Origem B - " + procId);

        // Atualizar status para simular finalização
        jdbcTemplate.update("UPDATE sgc.subprocesso SET situacao = 'MAPEAMENTO_MAPA_HOMOLOGADO' WHERE codigo = ?", subId);
        jdbcTemplate.update("UPDATE sgc.processo SET situacao = 'FINALIZADO' WHERE codigo = ?", procId);

        return processoFacade.buscarEntidadePorId(procId);
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e disponibilizado,
     * para acelerar cenários E2E que começam na validação do mapa.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-disponibilizado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaDisponibilizado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_DISPONIBILIZADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e validado,
     * para acelerar cenários E2E que começam no aceite final do mapa.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-validado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaValidado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_VALIDADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e homologado,
     * para acelerar cenários E2E que começam na finalização do processo.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-homologado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaHomologado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_HOMOLOGADO");
    }

    /**
     * Cria um processo de revisão já iniciado, com mapa preenchido e homologado,
     * para acelerar cenários E2E que começam após o encerramento da revisão.
     */
    @PostMapping("/fixtures/processo-revisao-com-mapa-homologado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisaoComMapaHomologado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoNaSituacao(request, TipoProcesso.REVISAO, "REVISAO_MAPA_HOMOLOGADO");
    }

    private Processo criarProcessoMapeamentoComMapaNaSituacao(ProcessoFixtureRequest request, String situacaoSubprocesso) {
        return criarProcessoNaSituacao(request, TipoProcesso.MAPEAMENTO, situacaoSubprocesso);
    }

    private Processo criarProcessoNaSituacao(ProcessoFixtureRequest request, TipoProcesso tipo, String situacaoSubprocesso) {
        ProcessoFixtureRequest requestIniciado = new ProcessoFixtureRequest(
                request.descricao(), request.unidadeSigla(), true, request.diasLimite());
        Processo processo = executeAsAdmin(() -> criarProcessoFixture(requestIniciado, tipo));

        Long procId = processo.getCodigo();
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Long unidId = unidade.getCodigo();
        Long subId = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ? AND unidade_codigo = ?",
                Long.class, procId, unidId);
        Long mapaId = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo = ?",
                Long.class, subId);

        String sufixo = " - " + procId;
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)",
                mapaId, "Atividade Fixture" + sufixo);
        Long atividadeId = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, mapaId, "Atividade Fixture" + sufixo);

        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) VALUES (?, ?)",
                atividadeId, "Conhecimento Fixture" + sufixo);
        jdbcTemplate.update("INSERT INTO sgc.competencia (mapa_codigo, descricao) VALUES (?, ?)",
                mapaId, "Competência Fixture" + sufixo);
        Long competenciaId = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.competencia WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, mapaId, "Competência Fixture" + sufixo);
        jdbcTemplate.update("INSERT INTO sgc.competencia_atividade (atividade_codigo, competencia_codigo) VALUES (?, ?)",
                atividadeId, competenciaId);
        jdbcTemplate.update("UPDATE sgc.subprocesso SET situacao = ? WHERE codigo = ?", situacaoSubprocesso, subId);

        return processoFacade.buscarEntidadePorId(procId);
    }

    /**
     * Executa uma operação com contexto de segurança ADMIN.
     */
    private <T> T executeAsAdmin(Supplier<T> operation) {
        var auth = new UsernamePasswordAuthenticationToken(
                "111111",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        var previousAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(auth);
            return operation.get();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previousAuth);
        }
    }

    /**
     * Método auxiliar para criar processos fixtures.
     */
    private Processo criarProcessoFixture(ProcessoFixtureRequest request, TipoProcesso tipo) {
        if (request.unidadeSigla().isBlank()) {
            throw new ErroValidacao("Unidade é obrigatória");
        }

        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());

        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        LocalDateTime dataLimite = LocalDate.now().plusDays(diasLimite).atStartOfDay();

        String descricao;
        String descReq = request.descricao();
        if (descReq != null && !descReq.isBlank()) {
            descricao = descReq;
        } else {
            descricao = "Processo Fixture E2E " + tipo.name() + " " + System.currentTimeMillis();
        }

        CriarProcessoRequest criarReq = CriarProcessoRequest.builder()
                .descricao(descricao)
                .tipo(tipo)
                .dataLimiteEtapa1(dataLimite)
                .unidades(List.of(unidade.getCodigo()))
                .build();

        Processo processo = processoFacade.criar(criarReq);

        if (Boolean.TRUE.equals(request.iniciar())) {
            List<Long> unidades = List.of(unidade.getCodigo());
            Long processoCodigo = processo.getCodigo();
            List<String> erros = processoFacade.iniciarProcesso(processoCodigo, unidades);

            if (!erros.isEmpty()) {
                throw new ErroValidacao("Falha ao iniciar processo fixture: " + String.join("; ", erros));
            }

            // Recarregar processo após iniciar
            processo = processoFacade.obterEntidadePorId(processoCodigo);
        }

        return processo;
    }

    /**
     * DTO para requisição de criação de processo fixture.
     */
    public record ProcessoFixtureRequest(
            @Nullable String descricao, String unidadeSigla, @Nullable Boolean iniciar, @Nullable Integer diasLimite) {
    }
}
