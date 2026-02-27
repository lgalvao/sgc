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
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
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
    private final OrganizacaoFacade organizacaoFacade;
    private final ResourceLoader resourceLoader;

    public E2eController(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate,
                         ProcessoFacade processoFacade, OrganizacaoFacade organizacaoFacade,
                         ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.processoFacade = processoFacade;
        this.organizacaoFacade = organizacaoFacade;
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

            // 1. Limpar Alertas
            jdbcTemplate.update("DELETE FROM sgc.alerta_usuario WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)", codigo);
            jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);

            // 2. Limpar dependentes de Mapa
            jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT codigo FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas);
            jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas);
            
            // Limpar referências de Mapa Vigente antes de excluir o Mapa
            jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE mapa_vigente_codigo IN " + subqueryMapas);
            jdbcTemplate.update("DELETE FROM sgc.mapa WHERE subprocesso_codigo IN " + subquerySubprocessos);

            // 3. Limpar dependentes de Subprocesso
            jdbcTemplate.update("DELETE FROM sgc.analise WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);

            // 4. Limpar UnidadeProcesso e Processo
            jdbcTemplate.update("DELETE FROM sgc.unidade_processo WHERE processo_codigo = ?", codigo);
            jdbcTemplate.update("DELETE FROM sgc.processo WHERE codigo = ?", codigo);
            
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            log.info("Limpeza robusta do processo {} concluída.", codigo);
        } catch (Exception e) {
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

        UnidadeDto unidade = organizacaoFacade.buscarPorSigla(request.unidadeSigla());

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
            List<String> erros;

            switch (tipo) {
                case TipoProcesso.MAPEAMENTO ->
                        erros = processoFacade.iniciarProcessoMapeamento(processoCodigo, unidades);
                case TipoProcesso.REVISAO -> erros = processoFacade.iniciarProcessoRevisao(processoCodigo, unidades);
                default -> erros = List.of();
            }

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
