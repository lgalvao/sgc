package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.ErroConfiguracao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/e2e")
@Profile("e2e")
@RequiredArgsConstructor
@Slf4j
public class E2eController {
    private static final String SQL_SUBPROCESSO_POR_PROCESSO = " sgc.subprocesso WHERE processo_codigo = ?)";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ProcessoFacade processoFacade;
    private final UnidadeFacade unidadeFacade;
    private final ResourceLoader resourceLoader;

    @PostMapping("/reset-database")
    public void resetDatabase() {
        log.info("Iniciando reset do banco de dados para E2E...");
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            executeDatabaseReset(conn);
            log.info("Reset do banco de dados concluído com sucesso.");
        } catch (Exception e) {
            log.error("Erro crítico ao resetar banco de dados", e);
            throw new ErroConfiguracao("Falha no reset do banco: " + e.getMessage());
        }
    }

    private void executeDatabaseReset(Connection conn) throws Exception {
        try (java.sql.Statement stmt = conn.createStatement()) {
            log.debug("Desabilitando integridade referencial");
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_SCHEMA) = 'SGC'",
                    String.class).stream().flatMap(java.util.stream.Stream::ofNullable).toList();

            log.info("Limpando {} tabelas no schema SGC", tables.size());
            for (String table : tables) {
                limparTabela(stmt, table);
            }

            Resource seedResource = getSeedResource();
            log.info("Executando script de seed: {}", seedResource.getFilename());
            ScriptUtils.executeSqlScript(conn, seedResource);

            log.debug("Reabilitando integridade referencial");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    private void limparTabela(java.sql.Statement stmt, String table) throws java.sql.SQLException {
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

    @PostMapping("/processo/{codigo}/limpar")
    @Transactional
    public void limparProcessoComDependentes(@PathVariable Long codigo) {
        String sqlMapas =
                "SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN (SELECT codigo FROM" + SQL_SUBPROCESSO_POR_PROCESSO;
        List<Long> mapaIds = jdbcTemplate.queryForList(sqlMapas, Long.class, codigo).stream()
                .flatMap(java.util.stream.Stream::ofNullable)
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
        jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);
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
    public ProcessoDto criarProcessoMapeamento(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.MAPEAMENTO));
    }

    /**
     * Cria um processo de revisão via API para testes E2E. Mais rápido que criar via UI.
     */
    @PostMapping("/fixtures/processo-revisao")
    @Transactional
    public ProcessoDto criarProcessoRevisao(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.REVISAO));
    }

    /**
     * Executa uma operação com contexto de segurança ADMIN.
     */
    private <T> T executeAsAdmin(Supplier<T> operation) {
        var auth = new UsernamePasswordAuthenticationToken(
                "e2e-admin",
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
    private ProcessoDto criarProcessoFixture(ProcessoFixtureRequest request, TipoProcesso tipo) {
        // Validar entrada
        if (request.unidadeSigla().isBlank()) {
            throw new ErroValidacao("Unidade é obrigatória");
        }

        // Buscar unidade pela sigla
        UnidadeDto unidade = unidadeFacade.buscarPorSigla(request.unidadeSigla());
        if (unidade == null) {
            throw new ErroEntidadeNaoEncontrada("Unidade", request.unidadeSigla());
        }

        // Calcular data limite
        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        LocalDateTime dataLimite = LocalDate.now().plusDays(diasLimite).atStartOfDay();

        // Criar requisição de processo
        String descricao;
        String descReq = request.descricao();
        if (descReq != null && !descReq.isBlank()) {
            descricao = descReq;
        } else {
            descricao = "Processo Fixture E2E " + tipo.name() + " " + System.currentTimeMillis();
        }

        CriarProcessoRequest criarReq =
                CriarProcessoRequest.builder()
                        .descricao(descricao)
                        .tipo(tipo)
                        .dataLimiteEtapa1(dataLimite)
                        .unidades(List.of(unidade.getCodigo()))
                        .build();

        // Criar processo
        ProcessoDto processo = processoFacade.criar(criarReq);

        // Iniciar se solicitado
        if (Boolean.TRUE.equals(request.iniciar())) {
            List<Long> unidades = List.of(unidade.getCodigo());
            Long processoCodigo = processo.getCodigo();
            List<String> erros;

            if (tipo == TipoProcesso.MAPEAMENTO) {
                erros = processoFacade.iniciarProcessoMapeamento(processoCodigo, unidades);
            } else if (tipo == TipoProcesso.REVISAO) {
                erros = processoFacade.iniciarProcessoRevisao(processoCodigo, unidades);
            } else {
                erros = List.of();
            }

            if (erros != null && !erros.isEmpty()) {
                throw new ErroValidacao("Falha ao iniciar processo fixture: " + String.join("; ", erros));
            }

            // Recarregar processo após iniciar
            processo =
                    processoFacade
                            .obterPorId(processoCodigo)
                            .orElseThrow(
                                    () ->
                                            new ErroEntidadeNaoEncontrada(
                                                    "Processo", processoCodigo));
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
