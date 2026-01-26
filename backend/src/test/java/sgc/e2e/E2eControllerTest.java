package sgc.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UnidadeFacade;
import sgc.processo.service.ProcessoFacade;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import sgc.organizacao.dto.UnidadeDto;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.ProcessoDto;

@Tag("integration")
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Testes do E2eController (Backend Support)")
class E2eControllerTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Mock
    private ProcessoFacade processoFacade;

    @Mock
    private UnidadeFacade unidadeFacade;

    @Mock
    private ResourceLoader resourceLoader;

    private E2eController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Default behavior for resource loader (file found in first path)
        mockResourceLoader("file:../e2e/setup/seed.sql", true);
        controller = new E2eController(jdbcTemplate, namedJdbcTemplate, dataSource, processoFacade, unidadeFacade, resourceLoader);
    }

    private void mockResourceLoader(String path, boolean exists) {
        Resource mockResource = org.mockito.Mockito.mock(Resource.class);
        when(mockResource.exists()).thenReturn(exists);
        if (exists) {
            try {
                when(mockResource.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("SELECT 1;".getBytes()));
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
        when(resourceLoader.getResource(path)).thenReturn(mockResource);
    }

    @Test
    @DisplayName("Deve limpar dados do processo e suas dependências")
    void deveLimparDadosDoProcessoComDependentes() {
        // Arrange
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

        // Act
        controller.limparProcessoComDependentes(100L);

        // Assert
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
    @DisplayName("Deve resetar o banco de dados truncando tabelas")
    void deveResetarBancoTruncandoTabelas() throws SQLException {
        // Arrange
        mockResourceLoader("file:../e2e/setup/seed.sql", true);

        jdbcTemplate.execute(
                "INSERT INTO sgc.vw_unidade (codigo, nome, sigla, tipo, situacao) VALUES (888, 'Reset"
                        + " Unit', 'RST', 'OPERACIONAL', 'ATIVA')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.processo (codigo, descricao) VALUES (888, 'Reset Proc')");

        assertCount("sgc.vw_unidade WHERE codigo=888", 1);
        assertCount("sgc.processo WHERE codigo=888", 1);

        // Act
        try {
            controller.resetDatabase();
        } catch (RuntimeException e) {
            // Ignored - pode ocorrer erro de integridade referencial se não limpar na ordem, mas o teste foca no truncate
            // Na implementação real do resetDatabase, ele desativa constraints.
        }

        // Assert
        assertCount("sgc.vw_unidade WHERE codigo=888", 0);
        assertCount("sgc.processo WHERE codigo=888", 0);
    }

    @Test
    @DisplayName("Deve usar segundo caminho se primeiro falhar para seed.sql")
    void deveUsarSegundoCaminhoParaSeedSql() throws SQLException {
        // Usa mocks de DB para focar na lógica de recursos e evitar erros de SQL
        JdbcTemplate mockJdbc = org.mockito.Mockito.mock(JdbcTemplate.class);
        NamedParameterJdbcTemplate mockNamed = org.mockito.Mockito.mock(NamedParameterJdbcTemplate.class);
        DataSource mockDs = org.mockito.Mockito.mock(DataSource.class);
        java.sql.Connection mockConn = org.mockito.Mockito.mock(java.sql.Connection.class);
        java.sql.Statement mockStmt = org.mockito.Mockito.mock(java.sql.Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        when(mockDs.getConnection()).thenReturn(mockConn);

        E2eController localController = new E2eController(mockJdbc, mockNamed, mockDs, processoFacade, unidadeFacade, resourceLoader);

        mockResourceLoader("file:../e2e/setup/seed.sql", false);
        mockResourceLoader("file:e2e/setup/seed.sql", true);

        localController.resetDatabase();
    }

    @Test
    @DisplayName("Deve lançar erro se seed.sql não encontrado em nenhum lugar")
    void deveLancarErroSeSeedNaoEncontrado() {
        // Usa mocks de DB
        JdbcTemplate mockJdbc = org.mockito.Mockito.mock(JdbcTemplate.class);
        E2eController localController = new E2eController(mockJdbc, namedJdbcTemplate, dataSource, processoFacade, unidadeFacade, resourceLoader);

        mockResourceLoader("file:../e2e/setup/seed.sql", false);
        mockResourceLoader("file:e2e/setup/seed.sql", false);

        org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroConfiguracao.class, () -> localController.resetDatabase());
    }

    private void assertCount(String tableAndWhere, int expected) {
        Integer count =
                jdbcTemplate.queryForObject("SELECT count(*) FROM " + tableAndWhere, Integer.class);
        assertEquals(expected, count, "Contagem incorreta para " + tableAndWhere);
    }

    @Test
    @DisplayName("Deve criar processo de mapeamento fixture com descrição padrão e não iniciado")
    void deveCriarProcessoMapeamentoFixture() {
        // Arrange
        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                null, "SIGLA", false, null);

        UnidadeDto un = new UnidadeDto();
        un.setCodigo(1L);
        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        ProcessoDto proc = new ProcessoDto();
        proc.setCodigo(100L);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(proc);

        // Act
        ProcessoDto result = controller.criarProcessoMapeamento(req);

        // Assert
        assertEquals(100L, result.getCodigo());
        verify(processoFacade).criar(any(CriarProcessoRequest.class));
    }

    @Test
    @DisplayName("Deve criar processo de revisão fixture com descrição informada e iniciado")
    void deveCriarProcessoRevisaoFixture() {
        // Arrange
        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                "Desc", "SIGLA", true, 10);

        UnidadeDto un = new UnidadeDto();
        un.setCodigo(1L);
        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        ProcessoDto proc = new ProcessoDto();
        proc.setCodigo(100L);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(proc);
        when(processoFacade.obterPorId(100L)).thenReturn(Optional.of(proc));

        // Act
        ProcessoDto result = controller.criarProcessoRevisao(req);

        // Assert
        assertEquals(100L, result.getCodigo());
        verify(processoFacade).iniciarProcessoRevisao(100L, List.of(1L));
    }

    @Test
    @DisplayName("Deve criar processo de mapeamento fixture e iniciar")
    void deveCriarProcessoMapeamentoFixtureIniciado() {
        // Arrange
        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                "Desc", "SIGLA", true, 10);

        UnidadeDto un = new UnidadeDto();
        un.setCodigo(1L);
        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        ProcessoDto proc = new ProcessoDto();
        proc.setCodigo(100L);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(proc);
        when(processoFacade.obterPorId(100L)).thenReturn(Optional.of(proc));

        // Act
        ProcessoDto result = controller.criarProcessoMapeamento(req);

        // Assert
        assertEquals(100L, result.getCodigo());
        verify(processoFacade).iniciarProcessoMapeamento(100L, List.of(1L));
    }

    @Test
    @DisplayName("Deve lidar com erro ao resetar banco")
    void deveLidarComErroAoResetarBanco() {
        JdbcTemplate mockJdbc = org.mockito.Mockito.mock(JdbcTemplate.class);
        org.mockito.Mockito.doThrow(new RuntimeException("Error")).when(mockJdbc).execute(any(String.class));

        E2eController localController = new E2eController(mockJdbc, namedJdbcTemplate, dataSource, processoFacade, unidadeFacade, resourceLoader);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> localController.resetDatabase());
    }

    @Test
    @DisplayName("Deve limpar processo sem dependentes")
    void deveLimparProcessoSemDependentes() {
        // Create only process, no subprocess/mapa
        jdbcTemplate.execute("INSERT INTO sgc.processo (codigo, descricao, situacao, tipo) VALUES (101, 'Proc Empty', 'CRIADO', 'MAPEAMENTO')");

        controller.limparProcessoComDependentes(101L);

        assertCount("sgc.processo WHERE codigo=101", 0);
    }
}
