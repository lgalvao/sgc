package sgc.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.service.SgrhService;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class E2eControllerTest { // Alterado aqui
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Mock
    private ProcessoService processoService;

    @Mock
    private SgrhService sgrhService;

    private E2eController controller; // Já está correto

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new E2eController(jdbcTemplate, dataSource, processoService, sgrhService);
    }

    @Test
    void shouldClearProcessData_RealDelete() {
        // 0. Limpar ambiente (data.sql polui o teste)
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.processo");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.subprocesso");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.mapa");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.atividade");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.competencia");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.alerta");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.alerta_usuario");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.unidade_processo");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.unidade");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.usuario");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        // 1. Setup: Inserir dados interdependentes para simular um processo completo
        
        // Unidade e Usuário (Base)
        jdbcTemplate.execute("INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao) VALUES (999, 'Teste Unit', 'TESTE', 'OPERACIONAL', 'ATIVA')");
        jdbcTemplate.execute("INSERT INTO sgc.usuario (titulo_eleitoral, nome) VALUES ('123', 'User Teste')");

        // Processo
        jdbcTemplate.execute("INSERT INTO sgc.processo (codigo, descricao, situacao, tipo) VALUES (100, 'Processo Teste', 'CRIADO', 'MAPEAMENTO')");
        
        // Mapa (vinculado a Unidade)
        jdbcTemplate.execute("INSERT INTO sgc.mapa (codigo, unidade_codigo) VALUES (200, 999)");
        
        // Subprocesso (vincula Processo, Unidade e Mapa)
        jdbcTemplate.execute("INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, mapa_codigo, situacao_id) VALUES (300, 100, 999, 200, 'NAO_INICIADO')");

        // Atividade (vincula Mapa)
        jdbcTemplate.execute("INSERT INTO sgc.atividade (codigo, mapa_codigo, descricao) VALUES (400, 200, 'Atividade Teste')");
        
        // Competência (vincula Mapa)
        jdbcTemplate.execute("INSERT INTO sgc.competencia (codigo, mapa_codigo, descricao) VALUES (450, 200, 'Competencia Teste')");

        // Alerta e Alerta Usuário (vinculam Processo e Usuário)
        jdbcTemplate.execute("INSERT INTO sgc.alerta (codigo, processo_codigo, unidade_destino_codigo, usuario_destino_titulo) VALUES (500, 100, 999, '123')");
        jdbcTemplate.execute("INSERT INTO sgc.alerta_usuario (alerta_codigo, usuario_titulo_eleitoral) VALUES (500, '123')");
        
        // Unidade Processo (vincula Processo)
        jdbcTemplate.execute("INSERT INTO sgc.unidade_processo (codigo, processo_codigo, unidade_codigo) VALUES (600, 100, 999)");

        // Verificar estado inicial
        assertCount("sgc.processo", 1);
        assertCount("sgc.subprocesso", 1);
        assertCount("sgc.mapa", 1);
        assertCount("sgc.atividade", 1);
        assertCount("sgc.competencia", 1);
        assertCount("sgc.alerta", 1);
        assertCount("sgc.alerta_usuario", 1);
        assertCount("sgc.unidade_processo", 1);

        // 2. Executar limpeza
        controller.limparProcessoComDependentes(100L);

        // 3. Verificar se tudo ligado ao processo foi removido
        assertCount("sgc.processo", 0);
        assertCount("sgc.subprocesso", 0);
        assertCount("sgc.mapa", 0);
        assertCount("sgc.atividade", 0);
        assertCount("sgc.competencia", 0);
        assertCount("sgc.alerta", 0);
        assertCount("sgc.alerta_usuario", 0);
        assertCount("sgc.unidade_processo", 0);
        
        // Dados globais devem permanecer
        assertCount("sgc.unidade", 1);
        assertCount("sgc.usuario", 1);
    }
    
    @Test
    void shouldResetDatabase_TruncateTables() throws SQLException {
        // Inserir dados em várias tabelas
        jdbcTemplate.execute("INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao) VALUES (888, 'Reset Unit', 'RST', 'OPERACIONAL', 'ATIVA')");
        jdbcTemplate.execute("INSERT INTO sgc.processo (codigo, descricao) VALUES (888, 'Reset Proc')");
        
        assertCount("sgc.unidade WHERE codigo=888", 1);
        assertCount("sgc.processo WHERE codigo=888", 1);
        
        try {
            controller.resetDatabase();
        } catch (RuntimeException e) {
            // Ignorar erro de arquivo seed.sql não encontrado (esperado em teste unitário isolado)
            // O foco aqui é se o TRUNCATE funcionou antes de tentar ler o arquivo
        }
        
        // Verificar se as tabelas foram limpas
        assertCount("sgc.unidade WHERE codigo=888", 0);
        assertCount("sgc.processo WHERE codigo=888", 0);
    }

    private void assertCount(String tableAndWhere, int expected) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM " + tableAndWhere, Integer.class);
        assertEquals(expected, count, "Contagem incorreta para " + tableAndWhere);
    }
}