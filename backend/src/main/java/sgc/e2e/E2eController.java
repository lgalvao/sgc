package sgc.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.api.CriarProcessoReq;
import sgc.processo.api.ProcessoDto;
import sgc.processo.api.model.TipoProcesso;
import sgc.processo.internal.service.ProcessoService;
import sgc.sgrh.api.UnidadeDto;
import sgc.sgrh.SgrhService;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/e2e")
@Profile("e2e")
@RequiredArgsConstructor
public class E2eController {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;
    private final ProcessoService processoService;
    private final SgrhService sgrhService;

    @PostMapping("/reset-database")
    public void resetDatabase() throws SQLException {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        try {
            List<String> tables =
                    jdbcTemplate.queryForList(
                            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA ="
                                    + " 'SGC'",
                            String.class);

            for (String table : tables) {
                // TRUNCATE is a DDL command, parameters are not supported here, but table names
                // come from trusted schema query.
                jdbcTemplate.execute("TRUNCATE TABLE sgc." + table);
            }
        } finally {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }

        File seedFile = new File("../e2e/setup/seed.sql");
        if (!seedFile.exists()) seedFile = new File("e2e/setup/seed.sql");
        if (!seedFile.exists()) throw new IllegalStateException("Arquivo seed.sql não encontrado");

        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(seedFile));
        }
    }

    @PostMapping("/processo/{codigo}/limpar")
    @Transactional
    public void limparProcessoComDependentes(@PathVariable Long codigo) {
        String sqlMapas =
                "SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN (SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ?)";
        List<Long> mapaIds = jdbcTemplate.queryForList(sqlMapas, Long.class, codigo);

        jdbcTemplate.update(
                "DELETE FROM sgc.analise WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + " sgc.subprocesso WHERE processo_codigo = ?)",
                codigo);
        jdbcTemplate.update(
                "DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + " sgc.subprocesso WHERE processo_codigo = ?)",
                codigo);
        jdbcTemplate.update(
                "DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + " sgc.subprocesso WHERE processo_codigo = ?)",
                codigo);

        if (!mapaIds.isEmpty()) {
            MapSqlParameterSource params = new MapSqlParameterSource("ids", mapaIds);

            namedParameterJdbcTemplate.update(
                    "DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM"
                            + " sgc.atividade WHERE mapa_codigo IN (:ids))",
                    params);

            namedParameterJdbcTemplate.update(
                    "DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo"
                            + " FROM sgc.atividade WHERE mapa_codigo IN (:ids))",
                    params);

            namedParameterJdbcTemplate.update(
                    "DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT"
                            + " codigo FROM sgc.competencia WHERE mapa_codigo IN (:ids))",
                    params);

            namedParameterJdbcTemplate.update(
                    "DELETE FROM sgc.atividade WHERE mapa_codigo IN (:ids)",
                    params);

            namedParameterJdbcTemplate.update(
                    "DELETE FROM sgc.competencia WHERE mapa_codigo IN (:ids)",
                    params);

            namedParameterJdbcTemplate.update(
                    "DELETE FROM sgc.mapa WHERE codigo IN (:ids)",
                    params);
        }

        jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);

        jdbcTemplate.update(
                "DELETE FROM sgc.alerta_usuario WHERE alerta_codigo IN (SELECT codigo FROM"
                        + " sgc.alerta WHERE processo_codigo = ?)",
                codigo);
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
    private <T> T executeAsAdmin(java.util.function.Supplier<T> operation) {
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
        if (request.unidadeSigla() == null || request.unidadeSigla().isBlank()) {
            throw new IllegalArgumentException("Unidade é obrigatória");
        }

        // Buscar unidade pela sigla
        UnidadeDto unidade =
                sgrhService
                        .buscarUnidadePorSigla(request.unidadeSigla())
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade com sigla "
                                                        + request.unidadeSigla()
                                                        + " não encontrada"));

        // Calcular data limite
        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        LocalDateTime dataLimite = LocalDate.now().plusDays(diasLimite).atStartOfDay();

        // Criar requisição de processo
        String descricao;
        if (request.descricao() != null && !request.descricao().isBlank()) {
            descricao = request.descricao();
        } else {
            descricao = "Processo Fixture E2E " + tipo.name() + " " + System.currentTimeMillis();
        }

        CriarProcessoReq criarReq =
                CriarProcessoReq.builder()
                        .descricao(descricao)
                        .tipo(tipo)
                        .dataLimiteEtapa1(dataLimite)
                        .unidades(List.of(unidade.getCodigo()))
                        .build();

        // Criar processo
        ProcessoDto processo = processoService.criar(criarReq);

        // Iniciar se solicitado
        if (request.iniciar() != null && request.iniciar()) {
            List<Long> unidades = List.of(unidade.getCodigo());
            Long processoCodigo = processo.getCodigo();

            if (tipo == TipoProcesso.MAPEAMENTO) {
                processoService.iniciarProcessoMapeamento(processoCodigo, unidades);
            } else if (tipo == TipoProcesso.REVISAO) {
                processoService.iniciarProcessoRevisao(processoCodigo, unidades);
            }

            // Recarregar processo após iniciar
            processo =
                    processoService
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
            String descricao, String unidadeSigla, Boolean iniciar, Integer diasLimite) {
    }
}
