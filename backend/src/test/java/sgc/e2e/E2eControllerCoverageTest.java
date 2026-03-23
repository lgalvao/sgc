package sgc.e2e;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;

import java.sql.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("E2eController - Cobertura adicional")
class E2eControllerCoverageTest {
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private NamedParameterJdbcTemplate namedJdbcTemplate;
    @Mock private ProcessoService processoService;
    @Mock private ProcessoRepo processoRepo;
    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private MapaRepo mapaRepo;
    @Mock private UnidadeService unidadeService;
    @Mock private UsuarioFacade usuarioFacade;
    @Mock private ResourceLoader resourceLoader;

    @InjectMocks
    private E2eController controller;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        controller = new E2eController(jdbcTemplate, namedJdbcTemplate, processoService, processoRepo, subprocessoRepo, mapaRepo, unidadeService, usuarioFacade, resourceLoader);
    }

    @Test
    @DisplayName("resetDatabase deve retornar imediatamente se dataSource for nulo")
    void deveRetornarSeDataSourceForNulo() {
        when(jdbcTemplate.getDataSource()).thenReturn(null);
        controller.resetDatabase();
        verify(jdbcTemplate).getDataSource();
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

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

        Processo dto = Processo.builder().codigo(100L).build();
        when(processoService.criar(any())).thenReturn(dto);
        when(processoService.buscarPorCodigo(100L)).thenReturn(dto);
        when(usuarioFacade.buscarPorLogin(anyString())).thenReturn(new Usuario());

        Subprocesso sub = new Subprocesso();
        sub.setCodigo(200L);
        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(100L, 10L)).thenReturn(Optional.of(sub));
        when(subprocessoRepo.findById(200L)).thenReturn(Optional.of(sub));

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        when(mapaRepo.buscarPorSubprocesso(200L)).thenReturn(Optional.of(mapa));

        when(processoRepo.findById(100L)).thenReturn(Optional.of(new Processo()));

        Processo result = controller.criarProcessoFinalizadoComAtividades(req);

        assertNotNull(result);
        // Verificar chamadas com diferentes números de argumentos
        verify(jdbcTemplate, atLeastOnce()).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("criarProcessoMapeamentoComMapaComSugestoes: Deve salvar sugestões no mapa")
    void deveCriarProcessoMapeamentoComMapaComSugestoes() {
        var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        Unidade superior = new Unidade();
        superior.setCodigo(5L);
        unidade.setUnidadeSuperior(superior);
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

        Processo dto = Processo.builder().codigo(100L).build();
        when(processoService.criar(any())).thenReturn(dto);
        when(processoService.buscarPorCodigo(100L)).thenReturn(dto);
        when(usuarioFacade.buscarPorLogin(anyString())).thenReturn(new Usuario());

        Subprocesso sub = new Subprocesso();
        sub.setCodigo(200L);
        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(100L, 10L)).thenReturn(Optional.of(sub));
        when(subprocessoRepo.findById(200L)).thenReturn(Optional.of(sub));

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        when(mapaRepo.buscarPorSubprocesso(200L)).thenReturn(Optional.of(mapa));

        Processo result = controller.criarProcessoMapeamentoComMapaComSugestoes(req);

        assertNotNull(result);
        assertEquals("Sugestão de ajuste na competência via fixture E2E", mapa.getSugestoes());
        verify(mapaRepo).save(mapa);
    }

    @Test
    @DisplayName("criarProcessoRevisaoComCadastroDisponibilizado: Deve criar revisão e registrar movimentação")
    void deveCriarProcessoRevisaoComCadastroDisponibilizado() {
        var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        Unidade superior = new Unidade();
        superior.setCodigo(5L);
        unidade.setUnidadeSuperior(superior);
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

        Processo dto = Processo.builder().codigo(100L).build();
        when(processoService.criar(any())).thenReturn(dto);
        when(processoService.buscarPorCodigo(anyLong())).thenReturn(dto);
        when(usuarioFacade.buscarPorLogin(anyString())).thenReturn(new Usuario());

        Subprocesso sub = new Subprocesso();
        sub.setCodigo(200L);
        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(anyLong(), anyLong())).thenReturn(Optional.of(sub));
        when(subprocessoRepo.findById(anyLong())).thenReturn(Optional.of(sub));

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        when(mapaRepo.buscarPorSubprocesso(anyLong())).thenReturn(Optional.of(mapa));

        when(processoRepo.findById(anyLong())).thenReturn(Optional.of(new Processo()));

        Processo result = controller.criarProcessoRevisaoComCadastroDisponibilizado(req);

        assertNotNull(result);
        verify(jdbcTemplate, atLeastOnce()).update(contains("INSERT INTO sgc.movimentacao"), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("descricaoFixture: Deve retornar descrição padrão se nula ou vazia")
    void deveCobrirDescricaoFixturePadrao() throws Exception {
        var method = E2eController.class.getDeclaredMethod("descricaoFixture",
                E2eController.ProcessoFixtureRequest.class, TipoProcesso.class);
        method.setAccessible(true);

        var req = new E2eController.ProcessoFixtureRequest(null, "SIGLA", false, 30);
        String desc = (String) method.invoke(controller, req, TipoProcesso.MAPEAMENTO);
        assertThat(desc).contains("Processo fixture E2E MAPEAMENTO");

        req = new E2eController.ProcessoFixtureRequest("  ", "SIGLA", false, 30);
        desc = (String) method.invoke(controller, req, TipoProcesso.REVISAO);
        assertThat(desc).contains("Processo fixture E2E REVISAO");
    }

    @Test
    @DisplayName("obterUsuarioParaIniciacao: Deve retornar principal se for Usuario")
    void deveCobrirObterUsuarioParaIniciacaoComUsuarioPrincipal() throws Exception {
        Usuario usuario = new Usuario();
        // Em vez de setLogin (que pode não existir), vamos assumir que o MockitoExtension limpa o contexto
        var auth = new UsernamePasswordAuthenticationToken(usuario, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            var method = E2eController.class.getDeclaredMethod("obterUsuarioParaIniciacao");
            method.setAccessible(true);

            Usuario result = (Usuario) method.invoke(controller);
            assertEquals(usuario, result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("criarProcessoMapeamentoComCadastroDisponibilizado: Deve chamar criarProcessoNaSituacao")
    void deveCriarProcessoMapeamentoComCadastroDisponibilizado() {
        var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        Unidade superior = new Unidade();
        superior.setCodigo(5L);
        unidade.setUnidadeSuperior(superior);
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(unidade);

        Processo dto = Processo.builder().codigo(100L).build();
        when(processoService.criar(any())).thenReturn(dto);
        when(processoService.buscarPorCodigo(anyLong())).thenReturn(dto);
        when(usuarioFacade.buscarPorLogin(anyString())).thenReturn(new Usuario());

        Subprocesso sub = new Subprocesso();
        sub.setCodigo(200L);
        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(anyLong(), anyLong())).thenReturn(Optional.of(sub));
        when(subprocessoRepo.findById(anyLong())).thenReturn(Optional.of(sub));

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        when(mapaRepo.buscarPorSubprocesso(anyLong())).thenReturn(Optional.of(mapa));

        Processo result = controller.criarProcessoMapeamentoComCadastroDisponibilizado(req);

        assertNotNull(result);
        verify(subprocessoRepo).findByProcessoCodigoAndUnidadeCodigo(anyLong(), anyLong());
    }

    @Test
    @DisplayName("limparTabela: Deve logar erro se falhar")
    void deveCobrirErroAoLimparTabela() throws Exception {
        Statement stmt = mock(Statement.class);
        doThrow(new SQLException("Erro simulado")).when(stmt).execute(anyString());

        var method = E2eController.class.getDeclaredMethod("limparTabela", Statement.class, String.class);
        method.setAccessible(true);

        // Não deve lançar exceção, apenas logar
        assertDoesNotThrow(() -> method.invoke(controller, stmt, "TABELA"));
    }
}
