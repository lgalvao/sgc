package sgc.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.service.SgrhService;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class E2eControllerTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Mock
    private ProcessoService processoService;

    @Mock
    private SgrhService sgrhService;

    private E2eController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new E2eController(jdbcTemplate, dataSource, processoService, sgrhService);
    }

    @Test
    void shouldClearProcessData_RealDelete() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.processo");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.subprocesso");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.mapa");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.atividade");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.competencia");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.alerta");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.alerta_usuario");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.unidade_processo");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.vw_unidade");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.vw_usuario");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        jdbcTemplate.execute(
                "INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao) VALUES (999, 'Teste"
                        + " Unit', 'TESTE', 'OPERACIONAL', 'ATIVA')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.vw_usuario (titulo, nome) VALUES ('123', 'User Teste')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.processo (codigo, descricao, situacao, tipo) VALUES (100,"
                        + " 'Processo Teste', 'CRIADO', 'MAPEAMENTO')");

        // Circular dependency handling: Subprocesso -> Mapa -> Subprocesso
        jdbcTemplate.execute(
                "INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao) VALUES (300, 100, 999, 'NAO_INICIADO')");

        jdbcTemplate.execute("INSERT INTO sgc.mapa (codigo, subprocesso_codigo) VALUES (200, 300)");

        jdbcTemplate.execute("UPDATE sgc.subprocesso SET mapa_codigo = 200 WHERE codigo = 300");

        jdbcTemplate.execute(
                "INSERT INTO sgc.atividade (codigo, mapa_codigo, descricao) VALUES (400, 200,"
                        + " 'Atividade Teste')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.competencia (codigo, mapa_codigo, descricao) VALUES (450, 200,"
                        + " 'Competencia Teste')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.alerta (codigo, processo_codigo, unidade_destino_codigo,"
                        + " usuario_destino_titulo) VALUES (500, 100, 999, '123')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.alerta_usuario (alerta_codigo, usuario_titulo) VALUES"
                        + " (500, '123')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.unidade_processo (processo_codigo, unidade_codigo) VALUES"
                        + " (100, 999)");

        assertCount("sgc.processo", 1);
        assertCount("sgc.subprocesso", 1);
        assertCount("sgc.mapa", 1);
        assertCount("sgc.atividade", 1);
        assertCount("sgc.competencia", 1);
        assertCount("sgc.alerta", 1);
        assertCount("sgc.alerta_usuario", 1);
        assertCount("sgc.unidade_processo", 1);

        assertCount("sgc.vw_unidade", 1);
        assertCount("sgc.vw_usuario", 1);

        controller.limparProcessoComDependentes(100L);

        assertCount("sgc.processo", 0);
        assertCount("sgc.subprocesso", 0);
        assertCount("sgc.mapa", 0);
        assertCount("sgc.atividade", 0);
        assertCount("sgc.competencia", 0);
        assertCount("sgc.alerta", 0);
        assertCount("sgc.alerta_usuario", 0);
        assertCount("sgc.unidade_processo", 0);

        assertCount("sgc.vw_unidade", 1);
        assertCount("sgc.vw_usuario", 1);
    }

    @Test
    void shouldResetDatabase_TruncateTables() throws SQLException {
        jdbcTemplate.execute(
                "INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao) VALUES (888, 'Reset"
                        + " Unit', 'RST', 'OPERACIONAL', 'ATIVA')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.processo (codigo, descricao) VALUES (888, 'Reset Proc')");

        assertCount("sgc.vw_unidade WHERE codigo=888", 1);
        assertCount("sgc.processo WHERE codigo=888", 1);

        try {
            controller.resetDatabase();
        } catch (RuntimeException e) {
            // Ignored
        }

        assertCount("sgc.vw_unidade WHERE codigo=888", 0);
        assertCount("sgc.processo WHERE codigo=888", 0);
    }

    private void assertCount(String tableAndWhere, int expected) {
        Integer count =
                jdbcTemplate.queryForObject("SELECT count(*) FROM " + tableAndWhere, Integer.class);
        assertEquals(expected, count, "Contagem incorreta para " + tableAndWhere);
    }
}
