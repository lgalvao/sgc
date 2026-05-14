package sgc.e2e;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.cache.*;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.security.core.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.jdbc.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;

import javax.sql.*;
import java.io.*;
import java.nio.charset.*;
import java.sql.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("integration")
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("Testes do E2eController (Backend support)")
@SuppressWarnings("NullAway.Init")
class E2eControllerTest {
    private static final String SCRIPT_SQL_MINIMO_VALIDO = "SELECT 1;";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @Mock
    private ProcessoService processoService;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CacheManager cacheManager;

    private E2eController controller;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mockResourceLoader("file:../e2e/setup/seed.sql", true);
        controller = new E2eController(jdbcTemplate, namedJdbcTemplate, processoService, processoRepo, subprocessoRepo, mapaRepo, unidadeService, resourceLoader, cacheManager);
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
                when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(SCRIPT_SQL_MINIMO_VALIDO.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        when(resourceLoader.getResource(path)).thenReturn(mockResource);
    }

    @Test
    @DisplayName("Deve limpar dados do processo e suas dependências")
    void deveLimparDadosDoProcessoComDependentes() {

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.processo");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.subprocesso");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.mapa");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.atividade");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.competencia");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.alerta");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.alerta_usuario");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.unidade_processo");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.VW_UNIDADE");
        jdbcTemplate.execute("TRUNCATE TABLE sgc.VW_USUARIO");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        jdbcTemplate.execute(
                "INSERT INTO sgc.VW_UNIDADE (codigo, nome, sigla, tipo, situacao) VALUES (999, 'Teste"
                        + " Unit', 'TESTE', 'OPERACIONAL', 'ATIVA')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.VW_USUARIO (titulo, nome) VALUES ('123', 'User teste')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.processo (codigo, descricao, situacao, tipo, data_criacao, data_limite) VALUES (100,"
                        + " 'Processo teste', 'CRIADO', 'MAPEAMENTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + 30)");

        jdbcTemplate.execute(
                "INSERT INTO sgc.subprocesso (codigo, processo_codigo, unidade_codigo, situacao, data_limite_etapa1) VALUES (300, 100, 999, 'NAO_INICIADO', CURRENT_TIMESTAMP)");

        jdbcTemplate.execute("INSERT INTO sgc.mapa (codigo, subprocesso_codigo) VALUES (200, 300)");

        jdbcTemplate.execute(
                "INSERT INTO sgc.atividade (codigo, mapa_codigo, descricao) VALUES (400, 200,"
                        + " 'Atividade teste')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.competencia (codigo, mapa_codigo, descricao) VALUES (450, 200,"
                        + " 'Competencia teste')");
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Deve resetar o banco de dados truncando tabelas")
    void deveResetarBancoTruncandoTabelas() {

        mockResourceLoader("file:../e2e/setup/seed.sql", true);

        jdbcTemplate.execute(
                "INSERT INTO sgc.VW_UNIDADE (codigo, nome, sigla, tipo, situacao) VALUES (888, 'Reset"
                        + " Unit', 'RST', 'OPERACIONAL', 'ATIVA')");
        jdbcTemplate.execute(
                "INSERT INTO sgc.processo (codigo, descricao, situacao, tipo, data_criacao, data_limite)"
                        + " VALUES (888, 'Reset proc', 'CRIADO', 'MAPEAMENTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + 30)");

        assertCount("sgc.vw_unidade WHERE codigo=888", 1);
        assertCount("sgc.processo WHERE codigo=888", 1);

        try {
            controller.resetDatabase();
        } catch (RuntimeException e) {
            // Na implementação real do resetDatabase, ele desativa constraints.
        }

        assertCount("sgc.vw_unidade WHERE codigo=888", 0);
        assertCount("sgc.processo WHERE codigo=888", 0);
    }

    @Test
    @DisplayName("Deve usar segundo caminho se primeiro falhar para seed.sql")
    void deveUsarSegundoCaminhoParaSeedSql() throws Exception {
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

        assertThatThrownBy(controller::resetDatabase)
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("Arquivo seed.sql não encontrado");

        verify(resourceLoader).getResource("file:../e2e/setup/seed.sql");
        verify(resourceLoader).getResource("file:e2e/setup/seed.sql");
    }

    @Test
    @DisplayName("Deve lançar erro se seed.sql não encontrado em nenhum lugar")
    void deveLancarErroSeSeedNaoEncontrado() {
        mockResourceLoader("file:../e2e/setup/seed.sql", false);
        mockResourceLoader("file:e2e/setup/seed.sql", false);

        assertThatThrownBy(controller::resetDatabase)
                .isInstanceOf(ErroConfiguracao.class);
    }

    private void assertCount(String tableAndWhere, int expected) {
        Integer count =
                jdbcTemplate.queryForObject("SELECT count(*) FROM " + tableAndWhere, Integer.class);
        assertThat(count).as("Contagem incorreta para " + tableAndWhere).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve criar processo de mapeamento fixture com descrição padrão e não iniciado")
    void deveCriarProcessoMapeamentoFixture() {

        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                null, "SIGLA", false, null);

        when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(1L);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoService.criar(any(CriarProcessoRequest.class))).thenReturn(proc);

        Processo result = controller.criarProcessoMapeamento(req);

        assertThat(result.getCodigo()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Deve criar processo de revisão fixture com descrição informada e iniciado")
    void deveCriarProcessoRevisaoFixture() {

        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                "Desc", "SIGLA", true, 10);

        when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(1L);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoService.criar(any(CriarProcessoRequest.class))).thenReturn(proc);
        when(processoService.buscarPorCodigo(100L)).thenReturn(proc);

        Processo result = controller.criarProcessoRevisao(req);

        assertThat(result.getCodigo()).isEqualTo(100L);
        verify(processoService).iniciar(eq(100L), eq(List.of(1L)));
    }

    @Test
    @DisplayName("Deve criar processo de mapeamento fixture e iniciar")
    void deveCriarProcessoMapeamentoFixtureIniciado() {

        E2eController.ProcessoFixtureRequest req = new E2eController.ProcessoFixtureRequest(
                "Desc", "SIGLA", true, 10);

        when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(1L);

        Processo proc = new Processo();
        proc.setCodigo(100L);
        when(processoService.criar(any(CriarProcessoRequest.class))).thenReturn(proc);
        when(processoService.buscarPorCodigo(100L)).thenReturn(proc);

        Processo result = controller.criarProcessoMapeamento(req);

        assertThat(result.getCodigo()).isEqualTo(100L);
        verify(processoService).iniciar(eq(100L), eq(List.of(1L)));
    }

    @Test
    @DisplayName("Deve lidar com erro ao resetar banco")
    void deveLidarComErroAoResetarBanco() throws Exception {
        JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
        DataSource mockDataSource = Mockito.mock(DataSource.class);
        when(mockJdbc.getDataSource()).thenReturn(mockDataSource);
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Error"));

        E2eController controllerComErro = new E2eController(mockJdbc, namedJdbcTemplate, processoService, processoRepo, subprocessoRepo, mapaRepo, unidadeService, resourceLoader, cacheManager);
        assertThatThrownBy(controllerComErro::resetDatabase)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve limpar processo sem dependentes")
    void deveLimparProcessoSemDependentes() {
        // Create only process, no subprocess/mapa
        jdbcTemplate.execute(
                "INSERT INTO sgc.processo (codigo, descricao, situacao, tipo, data_criacao, data_limite)"
                        + " VALUES (101, 'Proc empty', 'CRIADO', 'MAPEAMENTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + 30)");

        controller.limparProcessoComDependentes(101L);

        assertCount("sgc.processo WHERE codigo=101", 0);
    }

    @Test
    @DisplayName("Deve limpar processo completo (modo robusto) sem dados")
    void deveLimparProcessoCompleto_SemDados() {
        controller.limparProcessoCompleto(999L);
        assertCount("sgc.processo WHERE codigo=999", 0);
    }

    @Test
    @DisplayName("Deve limpar processo completo (modo robusto) com erro na conexão")
    void deveLimparProcessoCompleto_ErroConexao() throws SQLException {
        JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
        DataSource mockDataSource = Mockito.mock(DataSource.class);
        when(mockJdbc.getDataSource()).thenReturn(mockDataSource);
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Erro simulado na conexao"));

        E2eController controllerComErro = new E2eController(mockJdbc, namedJdbcTemplate, processoService, processoRepo, subprocessoRepo, mapaRepo, unidadeService, resourceLoader, cacheManager);

        assertThatThrownBy(() -> controllerComErro.limparProcessoCompleto(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na limpeza do processo");
    }

    @Test
    @DisplayName("Deve limpar processo completo (modo robusto) com dataSource null")
    void deveLimparProcessoCompleto_DataSourceNull() {
        JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
        when(mockJdbc.getDataSource()).thenReturn(null);

        E2eController controllerComErro = new E2eController(mockJdbc, namedJdbcTemplate, processoService, processoRepo, subprocessoRepo, mapaRepo, unidadeService, resourceLoader, cacheManager);

        // Deve retornar silenciosamente
        controllerComErro.limparProcessoCompleto(999L);
    }

    @Nested
    @DisplayName("Cobertura extra isolada")
    class CoberturaExtra {
        private JdbcTemplate jdbcTemplateMock;
        private ProcessoService processoServiceMock;
        private UnidadeService unidadeServiceMock;
        private ResourceLoader resourceLoaderMock;
        private UsuarioFacade usuarioFacadeMock;
        private E2eController controllerIsolado;

        @BeforeEach
        void setUp() {
            jdbcTemplateMock = mock(JdbcTemplate.class);
            var namedJdbcTemplateMock = mock(NamedParameterJdbcTemplate.class);
            processoServiceMock = mock(ProcessoService.class);
            unidadeServiceMock = mock(UnidadeService.class);
            resourceLoaderMock = mock(ResourceLoader.class);
            usuarioFacadeMock = mock(UsuarioFacade.class);
            var processoRepoMock = mock(ProcessoRepo.class);
            var subprocessoRepoMock = mock(SubprocessoRepo.class);
            var mapaRepoMock = mock(MapaRepo.class);
            CacheManager cacheManagerMock = mock(CacheManager.class);
            controllerIsolado = new E2eController(jdbcTemplateMock, namedJdbcTemplateMock, processoServiceMock, processoRepoMock, subprocessoRepoMock, mapaRepoMock, unidadeServiceMock, resourceLoaderMock, cacheManagerMock);
        }

        @Test
        @DisplayName("limparTabela: Deve usar DELETE para evitar bug do H2 com constraints")
        void limparTabela_UsaDelete() throws Exception {
            try (Connection conn = mock(Connection.class);
                 Statement stmt = mock(Statement.class)) {

                when(stmt.execute(anyString())).thenReturn(true);

                DataSource ds = mock(DataSource.class);
                when(jdbcTemplateMock.getDataSource()).thenAnswer(i -> ds);
                when(ds.getConnection()).thenReturn(conn);
                when(conn.createStatement()).thenReturn(stmt);
                when(jdbcTemplateMock.queryForList(anyString(), eq(String.class))).thenReturn(List.of("TABELA_TESTE"));

                Resource resource = mock(Resource.class);
                when(resourceLoaderMock.getResource(anyString())).thenReturn(resource);
                when(resource.exists()).thenReturn(true);
                when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(SCRIPT_SQL_MINIMO_VALIDO.getBytes(StandardCharsets.UTF_8)));

                controllerIsolado.resetDatabase();

                ArgumentCaptor<String> sqls = ArgumentCaptor.forClass(String.class);
                verify(stmt, atLeastOnce()).execute(sqls.capture());
                assertThat(sqls.getAllValues()).noneMatch(sql -> sql.contains("TRUNCATE"));
                assertThat(sqls.getAllValues()).anyMatch(sql -> sql.contains("DELETE FROM sgc.TABELA_TESTE"));
            }
        }

        @Test
        @DisplayName("criarProcessoFixture: Unidade não encontrada")
        void criarProcessoFixture_UnidadeNaoEncontrada() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", false, 30);
            when(unidadeServiceMock.buscarCodigoPorSigla("SIGLA")).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "SIGLA"));

            assertThatThrownBy(() -> controllerIsolado.criarProcessoMapeamento(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("criarProcessoFixture: Falha ao iniciar processo devolve ErroValidacao")
        void criarProcessoFixture_FalhaIniciar() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            when(unidadeServiceMock.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoServiceMock.criar(any())).thenReturn(dto);
            when(usuarioFacadeMock.buscarPorLogin(anyString())).thenReturn(new Usuario());

            doThrow(new ErroValidacao("Erro 1, Erro 2"))
                    .when(processoServiceMock).iniciar(eq(100L), anyList());

            assertThatThrownBy(() -> controllerIsolado.criarProcessoMapeamento(req))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Erro 1");
        }

        @Test
        @DisplayName("criarProcessoFixture: Falha ao recarregar processo")
        void criarProcessoFixture_FalhaRecarregar() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            when(unidadeServiceMock.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoServiceMock.criar(any())).thenReturn(dto);
            when(usuarioFacadeMock.buscarPorLogin(anyString())).thenReturn(new Usuario());

            doNothing().when(processoServiceMock).iniciar(eq(100L), anyList()); // Sucesso

            when(processoServiceMock.buscarPorCodigo(100L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 100L)); // Falha ao recarregar

            assertThatThrownBy(() -> controllerIsolado.criarProcessoMapeamento(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Cobertura adicional - instâncias isoladas")
    class CoberturaMockTest {
        private JdbcTemplate jdbcTemplate;
        private NamedParameterJdbcTemplate namedJdbcTemplate;
        private ProcessoService processoService;
        private ProcessoRepo processoRepo;
        private SubprocessoRepo subprocessoRepo;
        private MapaRepo mapaRepo;
        private UnidadeService unidadeService;
        private ResourceLoader resourceLoader;
        private CacheManager cacheManager;
        private E2eController controller;

        @BeforeEach
        void setUp() {
            jdbcTemplate = mock(JdbcTemplate.class);
            namedJdbcTemplate = mock(NamedParameterJdbcTemplate.class);
            processoService = mock(ProcessoService.class);
            processoRepo = mock(ProcessoRepo.class);
            subprocessoRepo = mock(SubprocessoRepo.class);
            mapaRepo = mock(MapaRepo.class);
            unidadeService = mock(UnidadeService.class);
            resourceLoader = mock(ResourceLoader.class);
            cacheManager = mock(CacheManager.class);
            SecurityContextHolder.clearContext();
            controller = new E2eController(jdbcTemplate, namedJdbcTemplate, processoService, processoRepo, subprocessoRepo, mapaRepo, unidadeService, resourceLoader, cacheManager);
        }

        @Test
        @DisplayName("resetDatabase deve retornar imediatamente se dataSource for nulo")
        void deveRetornarSeDataSourceForNulo() {
            when(jdbcTemplate.getDataSource()).thenReturn(null);
            controller.resetDatabase();
            verify(jdbcTemplate).getDataSource();
        }

        @Test
        @DisplayName("validarAmbienteE2e deve falhar quando DataSource está indisponível")
        void validarAmbienteE2eComDataSourceNulo() {
            when(jdbcTemplate.getDataSource()).thenReturn(null);

            assertThatThrownBy(() -> controller.validarAmbienteE2e())
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("DataSource indisponível");
        }

        @Test
        @DisplayName("validarAmbienteE2e deve falhar quando banco não é H2 em memória")
        void validarAmbienteE2eComBancoNaoH2() throws Exception {
            DataSource dataSource = mock(DataSource.class);
            Connection conexao = mock(Connection.class);
            DatabaseMetaData metaData = mock(DatabaseMetaData.class);
            when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(conexao);
            when(conexao.getMetaData()).thenReturn(metaData);
            when(metaData.getDatabaseProductName()).thenReturn("Oracle");
            when(metaData.getURL()).thenReturn("jdbc:oracle:thin:@localhost:1521/xe");

            assertThatThrownBy(() -> controller.validarAmbienteE2e())
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("requer H2 em memória");
        }

        @Test
        @DisplayName("validarAmbienteE2e deve aceitar conexão H2 válida")
        void validarAmbienteE2eComH2Valido() throws Exception {
            DataSource dataSource = mock(DataSource.class);
            Connection conexao = mock(Connection.class);
            DatabaseMetaData metaData = mock(DatabaseMetaData.class);
            when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(conexao);
            when(conexao.getMetaData()).thenReturn(metaData);
            when(metaData.getDatabaseProductName()).thenReturn("H2");
            when(metaData.getURL()).thenReturn("jdbc:h2:mem:testdb");

            assertThatCode(() -> controller.validarAmbienteE2e()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validarAmbienteE2e deve encapsular falha de conexão SQL")
        void validarAmbienteE2eComFalhaSql() throws Exception {
            DataSource dataSource = mock(DataSource.class);
            when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenThrow(new SQLException("falha sql"));

            assertThatThrownBy(() -> controller.validarAmbienteE2e())
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("Falha ao validar DataSource");
        }

        @Test
        @DisplayName("criarProcessoMapeamento deve falhar se unidadeSigla for vazia")
        void deveFalharSeUnidadeSiglaVazia() {
            E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                    "desc", "", false, 30);
            assertThatThrownBy(() -> controller.criarProcessoMapeamento(request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("Unidade é obrigatória");
        }

        @Test
        @DisplayName("criarProcessoMapeamentoComMapaDisponibilizado deve falhar sem unidade superior")
        void deveFalharSemUnidadeSuperiorObrigatoria() {
            E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest("desc", "SIGLA", true, 30);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("SIGLA");
            when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);
            when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);
            Processo processo = Processo.builder().codigo(100L).build();
            when(processoService.criar(any())).thenReturn(processo);
            when(processoService.buscarPorCodigo(anyLong())).thenReturn(processo);

            assertThatThrownBy(() -> controller.criarProcessoMapeamentoComMapaDisponibilizado(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem unidade superior");
        }

        @Test
        @DisplayName("criarProcessoMapeamentoComMapaDisponibilizado deve falhar quando SQL não retorna código gerado")
        void deveFalharQuandoCodigoGeradoNaoEhEncontrado() {
            E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest("desc", "SIGLA", true, 30);
            when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("SIGLA");
            Unidade superior = new Unidade();
            superior.setCodigo(5L);
            unidade.setUnidadeSuperior(superior);
            when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

            Processo processo = Processo.builder().codigo(100L).build();
            when(processoService.criar(any())).thenReturn(processo);
            when(processoService.buscarPorCodigo(anyLong())).thenReturn(processo);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(200L);
            when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(anyLong(), anyLong())).thenReturn(Optional.of(subprocesso));
            when(subprocessoRepo.findById(anyLong())).thenReturn(Optional.of(subprocesso));

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            when(mapaRepo.buscarPorSubprocesso(anyLong())).thenReturn(Optional.of(mapa));

            when(jdbcTemplate.queryForObject(startsWith("SELECT codigo FROM sgc.atividade"), eq(Long.class), any(), any()))
                    .thenReturn(null);

            assertThatThrownBy(() -> controller.criarProcessoMapeamentoComMapaDisponibilizado(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não encontrou codigo gerado");
        }

        @Test
        @DisplayName("resetDatabase deve continuar quando cache não existir")
        void resetDatabaseComCacheNulo() throws Exception {
            DataSource dataSource = mock(DataSource.class);
            Connection connection = mock(Connection.class);
            Statement statement = mock(Statement.class);
            Resource seed = mock(Resource.class);
            CacheManager cacheManagerMock = mock(CacheManager.class);

            when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.getUpdateCount()).thenReturn(-1);
            when(statement.getMoreResults()).thenReturn(false);
            when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(List.of());
            when(resourceLoader.getResource(anyString())).thenReturn(seed);
            when(seed.exists()).thenReturn(true);
            when(seed.getInputStream()).thenReturn(new ByteArrayInputStream(SCRIPT_SQL_MINIMO_VALIDO.getBytes(StandardCharsets.UTF_8)));
            when(cacheManagerMock.getCacheNames()).thenReturn(List.of("cache-inexistente"));
            when(cacheManagerMock.getCache("cache-inexistente")).thenReturn(null);

            E2eController controllerComCacheNulo = new E2eController(
                    jdbcTemplate, namedJdbcTemplate, processoService, processoRepo, subprocessoRepo, mapaRepo, unidadeService, resourceLoader, cacheManagerMock);

            assertThatCode(controllerComCacheNulo::resetDatabase).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("criarProcessoRevisaoComMapaHomologado - erro validacao")
        void deveFalharRevisaoHomologadoSemUnidade() {
            E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest("desc", "", false, 30);
            assertThatThrownBy(() -> controller.criarProcessoRevisaoComMapaHomologado(request))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("criarProcessoRevisaoComCadastroHomologado - erro validacao")
        void deveFalharRevisaoCadastroHomologadoSemUnidade() {
            E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest("desc", " ", false, 30);
            assertThatThrownBy(() -> controller.criarProcessoRevisaoComCadastroHomologado(request))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("criarProcessoFinalizadoComAtividades: Deve criar processo e inserir atividades via SQL")
        void deveCriarProcessoFinalizadoComAtividades() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoService.criar(any())).thenReturn(dto);
            when(processoService.buscarPorCodigo(100L)).thenReturn(dto);

            Subprocesso sub = new Subprocesso();
            sub.setCodigo(200L);
            when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(100L, 10L)).thenReturn(Optional.of(sub));
            when(subprocessoRepo.findById(200L)).thenReturn(Optional.of(sub));

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            when(mapaRepo.buscarPorSubprocesso(200L)).thenReturn(Optional.of(mapa));

            when(processoRepo.findById(100L)).thenReturn(Optional.of(new Processo()));

            Processo result = controller.criarProcessoFinalizadoComAtividades(req);

            assertThat(result).isNotNull();
            verify(jdbcTemplate, atLeastOnce()).update(anyString(), any(Object[].class));
        }

        @Test
        @DisplayName("criarProcessoMapeamentoComMapaComSugestoes: Deve salvar sugestões no mapa")
        void deveCriarProcessoMapeamentoComMapaComSugestoes() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            Unidade superior = new Unidade();
            superior.setCodigo(5L);
            unidade.setUnidadeSuperior(superior);
            when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoService.criar(any())).thenReturn(dto);
            when(processoService.buscarPorCodigo(100L)).thenReturn(dto);

            Subprocesso sub = new Subprocesso();
            sub.setCodigo(200L);
            when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(100L, 10L)).thenReturn(Optional.of(sub));
            when(subprocessoRepo.findById(200L)).thenReturn(Optional.of(sub));

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            when(mapaRepo.buscarPorSubprocesso(200L)).thenReturn(Optional.of(mapa));
            when(jdbcTemplate.queryForObject(startsWith("SELECT codigo FROM sgc.atividade"), eq(Long.class), any(), any()))
                    .thenReturn(400L);
            when(jdbcTemplate.queryForObject(startsWith("SELECT codigo FROM sgc.competencia"), eq(Long.class), any(), any()))
                    .thenReturn(500L);

            Processo result = controller.criarProcessoMapeamentoComMapaComSugestoes(req);

            assertThat(result).isNotNull();
            assertThat(mapa.getSugestoes()).isEqualTo("Sugestão de ajuste na competência via fixture E2E");
            verify(mapaRepo).save(mapa);
        }

        @Test
        @DisplayName("criarProcessoRevisaoComCadastroDisponibilizado: Deve criar revisão e registrar movimentação")
        void deveCriarProcessoRevisaoComCadastroDisponibilizado() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            Unidade superior = new Unidade();
            superior.setCodigo(5L);
            unidade.setUnidadeSuperior(superior);
            when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoService.criar(any())).thenReturn(dto);
            when(processoService.buscarPorCodigo(anyLong())).thenReturn(dto);

            Subprocesso sub = new Subprocesso();
            sub.setCodigo(200L);
            when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(anyLong(), anyLong())).thenReturn(Optional.of(sub));
            when(subprocessoRepo.findById(anyLong())).thenReturn(Optional.of(sub));

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            when(mapaRepo.buscarPorSubprocesso(anyLong())).thenReturn(Optional.of(mapa));
            when(jdbcTemplate.queryForObject(startsWith("SELECT codigo FROM sgc.atividade"), eq(Long.class), any(), any()))
                    .thenReturn(400L);

            when(processoRepo.findById(anyLong())).thenReturn(Optional.of(new Processo()));

            Processo result = controller.criarProcessoRevisaoComCadastroDisponibilizado(req);

            assertThat(result).isNotNull();
            verify(jdbcTemplate, atLeastOnce()).update(contains("INSERT INTO sgc.movimentacao"), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("criarProcessoMapeamentoComCadastroDisponibilizado: Deve chamar criarProcessoNaSituacao")
        void deveCriarProcessoMapeamentoComCadastroDisponibilizado() {
            var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

            when(unidadeService.buscarCodigoPorSigla("SIGLA")).thenReturn(10L);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            Unidade superior = new Unidade();
            superior.setCodigo(5L);
            unidade.setUnidadeSuperior(superior);
            when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

            Processo dto = Processo.builder().codigo(100L).build();
            when(processoService.criar(any())).thenReturn(dto);
            when(processoService.buscarPorCodigo(anyLong())).thenReturn(dto);

            Subprocesso sub = new Subprocesso();
            sub.setCodigo(200L);
            when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(anyLong(), anyLong())).thenReturn(Optional.of(sub));
            when(subprocessoRepo.findById(anyLong())).thenReturn(Optional.of(sub));

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            when(mapaRepo.buscarPorSubprocesso(anyLong())).thenReturn(Optional.of(mapa));
            when(jdbcTemplate.queryForObject(startsWith("SELECT codigo FROM sgc.atividade"), eq(Long.class), any(), any()))
                    .thenReturn(400L);
            when(jdbcTemplate.queryForObject(startsWith("SELECT codigo FROM sgc.competencia"), eq(Long.class), any(), any()))
                    .thenReturn(500L);

            Processo result = controller.criarProcessoMapeamentoComCadastroDisponibilizado(req);

            assertThat(result).isNotNull();
            verify(subprocessoRepo).findByProcessoCodigoAndUnidadeCodigo(anyLong(), anyLong());
        }

        @Test
        @DisplayName("resetDatabase: deve continuar reset quando uma exclusão falhar")
        void deveContinuarResetQuandoUmaExclusaoFalhar() throws Exception {
            JdbcTemplate jdbcTemplateMock = mock(JdbcTemplate.class);
            DataSource dataSourceMock = mock(DataSource.class);
            Connection connectionMock = mock(Connection.class);
            Statement statementMock = mock(Statement.class);
            ResourceLoader resourceLoaderMock = mock(ResourceLoader.class);
            Resource resourceMock = mock(Resource.class);
            CacheManager cacheManagerMock = mock(CacheManager.class);

            when(jdbcTemplateMock.getDataSource()).thenReturn(dataSourceMock);
            when(dataSourceMock.getConnection()).thenReturn(connectionMock);
            when(connectionMock.createStatement()).thenReturn(statementMock);
            when(jdbcTemplateMock.queryForList(anyString(), eq(String.class))).thenReturn(List.of("TABELA_TESTE"));
            when(statementMock.execute(anyString())).thenAnswer(invocacao -> {
                String sql = invocacao.getArgument(0);
                if ("DELETE FROM sgc.TABELA_TESTE".equals(sql)) {
                    throw new SQLException("Erro simulado");
                }
                return true;
            });
            when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
            when(resourceMock.exists()).thenReturn(true);
            when(resourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(SCRIPT_SQL_MINIMO_VALIDO.getBytes(StandardCharsets.UTF_8)));

            E2eController controllerComMocks = new E2eController(
                    jdbcTemplateMock,
                    namedJdbcTemplate,
                    processoService,
                    processoRepo,
                    subprocessoRepo,
                    mapaRepo,
                    unidadeService,
                    resourceLoaderMock,
                    cacheManagerMock);

            assertThatCode(controllerComMocks::resetDatabase).doesNotThrowAnyException();
            verify(statementMock).execute("DELETE FROM sgc.TABELA_TESTE");
            verify(statementMock).execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
}
