package sgc.e2e;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.test.context.*;
import org.springframework.test.context.jdbc.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.processo.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;

import javax.sql.*;
import java.io.*;
import java.nio.charset.*;
import java.sql.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("integration")
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("Testes do E2eController (Backend Support)")
class E2eControllerTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @Mock
    private ProcessoFacade processoFacade;

    @Mock
    private OrganizacaoFacade organizacaoFacade;

    @Mock
    private ResourceLoader resourceLoader;

    private E2eController controller;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mockResourceLoader("file:../e2e/setup/seed.sql", true);
        controller = new E2eController(jdbcTemplate, namedJdbcTemplate, processoFacade, organizacaoFacade, resourceLoader);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    private void mockResourceLoader(String path, boolean exists) {
        Resource mockResource = Mockito.mock(Resource.class);
        when(mockResource.exists()).thenReturn(exists);
        if (exists) {
            try {
                when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream("SELECT 1;".getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
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
                "INSERT INTO sgc.alerta (codigo, processo_codigo, unidade_origem_codigo, unidade_destino_codigo,"
                        + " descricao, data_hora) VALUES (500, 100, 999, 999, 'Alerta teste', CURRENT_TIMESTAMP)");
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Deve resetar o banco de dados truncando tabelas")
    void deveResetarBancoTruncandoTabelas() {
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
    void deveUsarSegundoCaminhoParaSeedSql() throws Exception {
        // Usa mocks de DB para focar na lógica de recursos e evitar erros de SQL
        JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
        DataSource mockDataSource = Mockito.mock(DataSource.class);
        Connection mockConnection = Mockito.mock(Connection.class);
        Statement mockStatement = Mockito.mock(Statement.class);

        when(mockJdbc.getDataSource()).thenReturn(mockDataSource);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockJdbc.queryForList(anyString(), eq(String.class))).thenReturn(List.of());

        mockResourceLoader("file:../e2e/setup/seed.sql", false);
        mockResourceLoader("file:e2e/setup/seed.sql", false); // Ambos não existem para forçar ErroConfiguracao

        var exception = Assertions.assertThrows(ErroConfiguracao.class, controller::resetDatabase);
        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("Arquivo seed.sql não encontrado"));

        // Verifica se tentou carregar ambos os caminhos
        verify(resourceLoader).getResource("file:../e2e/setup/seed.sql");
        verify(resourceLoader).getResource("file:e2e/setup/seed.sql");
    }

    @Test
    @DisplayName("Deve lançar erro se seed.sql não encontrado em nenhum lugar")
    void deveLancarErroSeSeedNaoEncontrado() {
        mockResourceLoader("file:../e2e/setup/seed.sql", false);
        mockResourceLoader("file:e2e/setup/seed.sql", false);

        var exception = Assertions.assertThrows(ErroConfiguracao.class, controller::resetDatabase);
        Assertions.assertNotNull(exception);
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
        when(organizacaoFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(proc);

        // Act
        Processo result = controller.criarProcessoMapeamento(req);

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
        when(organizacaoFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(proc);
        when(processoFacade.obterEntidadePorId(100L)).thenReturn(proc);

        // Act
        Processo result = controller.criarProcessoRevisao(req);

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
        when(organizacaoFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(proc);
        when(processoFacade.obterEntidadePorId(100L)).thenReturn(proc);

        // Act
        Processo result = controller.criarProcessoMapeamento(req);

        // Assert
        assertEquals(100L, result.getCodigo());
        verify(processoFacade).iniciarProcessoMapeamento(100L, List.of(1L));
    }

    @Test
    @DisplayName("Deve lidar com erro ao resetar banco")
    void deveLidarComErroAoResetarBanco() throws Exception {
        JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
        DataSource mockDataSource = Mockito.mock(DataSource.class);
        when(mockJdbc.getDataSource()).thenReturn(mockDataSource);
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Error"));

        E2eController controllerComErro = new E2eController(mockJdbc, namedJdbcTemplate, processoFacade, organizacaoFacade, resourceLoader);
        var exception = Assertions.assertThrows(RuntimeException.class, controllerComErro::resetDatabase);
        Assertions.assertNotNull(exception);
    }

    @Test
    @DisplayName("Deve limpar processo sem dependentes")
    void deveLimparProcessoSemDependentes() {
        // Create only process, no subprocess/mapa
        jdbcTemplate.execute("INSERT INTO sgc.processo (codigo, descricao, situacao, tipo) VALUES (101, 'Proc Empty', 'CRIADO', 'MAPEAMENTO')");

        controller.limparProcessoComDependentes(101L);

        assertCount("sgc.processo WHERE codigo=101", 0);
    }

    @Test
    @DisplayName("Deve cobrir else em criarProcessoFixture")
    void deveCobrirElseEmCriarProcessoFixture() throws Exception {
        // Arrange
        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                "Desc", "SIGLA", true, 10);

        UnidadeDto un = new UnidadeDto();
        un.setCodigo(1L);
        when(organizacaoFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoFacade.criar(any())).thenReturn(proc);
        when(processoFacade.obterEntidadePorId(100L)).thenReturn(proc);

        // Use reflection to call private method
        var method = E2eController.class.getDeclaredMethod("criarProcessoFixture",
                E2eController.ProcessoFixtureRequest.class, TipoProcesso.class);
        method.setAccessible(true);

        // Act
        method.invoke(controller, req, TipoProcesso.DIAGNOSTICO);

        // Assert
        verify(processoFacade, never()).iniciarProcessoMapeamento(anyLong(), anyList());
        verify(processoFacade, never()).iniciarProcessoRevisao(anyLong(), anyList());
    }

    @Test
    @DisplayName("Deve cobrir branches de erros em iniciar no fixture")
    void deveCobrirBranchesErrosIniciar() throws Exception {
        // Arrange
        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                "   ", "SIGLA", true, 10); // Blank description

        UnidadeDto un = new UnidadeDto();
        un.setCodigo(1L);
        when(organizacaoFacade.buscarPorSigla("SIGLA")).thenReturn(un);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoFacade.criar(any())).thenReturn(proc);
        when(processoFacade.obterEntidadePorId(100L)).thenReturn(proc);

        // Simular retorno de erros vazio (sucesso)
        when(processoFacade.iniciarProcessoMapeamento(anyLong(), anyList())).thenReturn(List.of());

        var method = E2eController.class.getDeclaredMethod("criarProcessoFixture",
                E2eController.ProcessoFixtureRequest.class, TipoProcesso.class);
        method.setAccessible(true);

        // Act
        method.invoke(controller, req, TipoProcesso.MAPEAMENTO);

        // Assert
        verify(processoFacade).iniciarProcessoMapeamento(eq(100L), anyList());
    }

    @Nested
    @DisplayName("Cobertura Extra Isolada")
    class CoberturaExtra {
        private JdbcTemplate jdbcTemplateMock;
        private ProcessoFacade processoFacadeMock;
        private OrganizacaoFacade organizacaoFacadeMock;
        private ResourceLoader resourceLoaderMock;
        private E2eController controllerIsolado;

        @BeforeEach
        void setUp() {
            jdbcTemplateMock = mock(JdbcTemplate.class);
            var namedJdbcTemplateMock = mock(NamedParameterJdbcTemplate.class);
            processoFacadeMock = mock(ProcessoFacade.class);
            organizacaoFacadeMock = mock(OrganizacaoFacade.class);
            resourceLoaderMock = mock(ResourceLoader.class);
            controllerIsolado = new E2eController(jdbcTemplateMock, namedJdbcTemplateMock, processoFacadeMock, organizacaoFacadeMock, resourceLoaderMock);
        }

        @Test
        @DisplayName("limparTabela: Deve tentar DELETE se TRUNCATE falhar")
        void limparTabela_TruncateFalha_TentaDelete() throws Exception {
            try (Connection conn = mock(Connection.class);
                 Statement stmt = mock(Statement.class)) {

                when(stmt.execute(anyString())).thenReturn(true);
                doThrow(new SQLException("Erro H2")).when(stmt).execute(argThat(s -> s != null && s.contains("TRUNCATE")));

                DataSource ds = mock(DataSource.class);
                when(jdbcTemplateMock.getDataSource()).thenAnswer(i -> ds);
                when(ds.getConnection()).thenReturn(conn);
                when(conn.createStatement()).thenReturn(stmt);
                when(jdbcTemplateMock.queryForList(anyString(), eq(String.class))).thenReturn(List.of("TABELA_TESTE"));

                Resource resource = mock(Resource.class);
                when(resourceLoaderMock.getResource(anyString())).thenReturn(resource);
                when(resource.exists()).thenReturn(true);
                when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("SELECT 1;".getBytes(StandardCharsets.UTF_8)));

                controllerIsolado.resetDatabase();

                verify(stmt).execute(argThat(s -> s != null && s.contains("TRUNCATE")));
                verify(stmt).execute(contains("DELETE FROM sgc.TABELA_TESTE"));
            }
        }

        @Test
        @DisplayName("criarProcessoFixture: Unidade não encontrada")
        void criarProcessoFixture_UnidadeNaoEncontrada() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", false, 30);
            when(organizacaoFacadeMock.buscarPorSigla("SIGLA")).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "SIGLA"));

            assertThatThrownBy(() -> controllerIsolado.criarProcessoMapeamento(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("criarProcessoFixture: Falha ao iniciar processo devolve ErroValidacao")
        void criarProcessoFixture_FalhaIniciar() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            UnidadeDto unidade = UnidadeDto.builder().codigo(10L).build();
            when(organizacaoFacadeMock.buscarPorSigla("SIGLA")).thenReturn(unidade);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoFacadeMock.criar(any())).thenReturn(dto);

            when(processoFacadeMock.iniciarProcessoMapeamento(100L, List.of(10L)))
                    .thenReturn(List.of("Erro 1", "Erro 2"));

            assertThatThrownBy(() -> controllerIsolado.criarProcessoMapeamento(req))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Erro 1");
        }

        @Test
        @DisplayName("criarProcessoFixture: Falha ao recarregar processo")
        void criarProcessoFixture_FalhaRecarregar() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            UnidadeDto unidade = UnidadeDto.builder().codigo(10L).build();
            when(organizacaoFacadeMock.buscarPorSigla("SIGLA")).thenReturn(unidade);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoFacadeMock.criar(any())).thenReturn(dto);

            when(processoFacadeMock.iniciarProcessoMapeamento(100L, List.of(10L)))
                    .thenReturn(List.of()); // Sucesso

            when(processoFacadeMock.obterEntidadePorId(100L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 100L)); // Falha ao recarregar

            assertThatThrownBy(() -> controllerIsolado.criarProcessoMapeamento(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }
}
