package sgc.integracao;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.seguranca.login.dto.AutenticarRequest;
import sgc.seguranca.login.dto.AutorizarRequest;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.util.TestUtil;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-01: Realizar Login")
class CDU01IntegrationTest extends BaseIntegrationTest {
    private static final String BASE_URL = "/api/usuarios";

    @Autowired
    private TestUtil testUtil;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidadeAdmin;
    private Unidade unidadeGestor;
    private Usuario usuarioAdmin;
    private Usuario usuarioGestor;

    @BeforeEach
    void setUp() {

        // Setup Unidade Admin
        unidadeAdmin = UnidadeFixture.unidadePadrao();
        unidadeAdmin.setCodigo(null);
        unidadeAdmin.setSigla("ADM-UNIT-TEST");
        unidadeAdmin.setNome("Unidade Admin Teste");
        unidadeAdmin = unidadeRepo.saveAndFlush(unidadeAdmin);

        // Setup Usuario Admin
        usuarioAdmin = UsuarioFixture.usuarioComPerfil(unidadeAdmin, Perfil.ADMIN);
        usuarioAdmin.setTituloEleitoral("999999990001");
        usuarioAdmin.setNome("Admin User Teste");
        usuarioAdmin = usuarioRepo.saveAndFlush(usuarioAdmin);

        // Persist Perfil via JDBC explicitly
        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)",
                usuarioAdmin.getTituloEleitoral(), "ADMIN", unidadeAdmin.getCodigo());

        // Setup Unidade Gestor
        unidadeGestor = UnidadeFixture.unidadePadrao();
        unidadeGestor.setCodigo(null);
        unidadeGestor.setSigla("GES-UNIT-TEST");
        unidadeGestor.setNome("Unidade Gestor Teste");
        unidadeGestor = unidadeRepo.saveAndFlush(unidadeGestor);

        // Setup Usuario Gestor
        usuarioGestor = UsuarioFixture.usuarioPadrao();
        usuarioGestor.setTituloEleitoral("999999990002");
        usuarioGestor.setNome("Gestor User Teste");
        usuarioGestor = usuarioRepo.saveAndFlush(usuarioGestor);

        // Persist Perfis via JDBC explicitly (ADMIN na unidadeAdmin, GESTOR na
        // unidadeGestor)
        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)",
                usuarioGestor.getTituloEleitoral(), "ADMIN", unidadeAdmin.getCodigo());
        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)",
                usuarioGestor.getTituloEleitoral(), "GESTOR", unidadeGestor.getCodigo());

        entityManager.clear(); // Clear cache to ensure subsequent reads fetch fresh data including profiles

        // Reload entities to ensure they are managed and up-to-date
        unidadeAdmin = unidadeRepo.findById(unidadeAdmin.getCodigo()).orElseThrow();
        unidadeGestor = unidadeRepo.findById(unidadeGestor.getCodigo()).orElseThrow();
        usuarioAdmin = usuarioRepo.findById(usuarioAdmin.getTituloEleitoral()).orElseThrow();
        usuarioGestor = usuarioRepo.findById(usuarioGestor.getTituloEleitoral()).orElseThrow();
    }

    @Nested
    @DisplayName("Testes de fluxo de login completo")
    class FluxoLoginTests {
        @Test
        @DisplayName("Deve realizar login completo para usuário com um único perfil")
        void testLoginCompleto_sucessoUsuarioUnicoPerfil() throws Exception {
            String tituloEleitoral = usuarioAdmin.getTituloEleitoral();
            String senha = "password";
            AutenticarRequest authRequest = AutenticarRequest.builder().tituloEleitoral(tituloEleitoral)
                    .senha(senha).build();

            Cookie[] cookies = mockMvc.perform(post(BASE_URL + "/autenticar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true))
                    .andReturn().getResponse().getCookies();

            AutorizarRequest autorizarReq = AutorizarRequest.builder().tituloEleitoral(tituloEleitoral)
                    .build();

            mockMvc.perform(post(BASE_URL + "/autorizar")
                            .with(csrf())
                            .cookie(cookies)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(autorizarReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].perfil").value("ADMIN"))
                    .andExpect(jsonPath("$[0].unidade.sigla").value(unidadeAdmin.getSigla()));

            // Act & Assert: Etapa 3 - Entrar
            EntrarRequest entrarReq = EntrarRequest.builder()
                    .tituloEleitoral(tituloEleitoral)
                    .perfil("ADMIN")
                    .unidadeCodigo(unidadeAdmin.getCodigo())
                    .build();

            mockMvc.perform(post(BASE_URL + "/entrar")
                            .with(csrf())
                            .cookie(cookies)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(entrarReq)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve realizar login completo para usuário com múltiplos perfis")
        void testLoginCompleto_sucessoUsuarioMultiplosPerfis() throws Exception {
            String tituloEleitoral = usuarioGestor.getTituloEleitoral();
            String senha = "password";
            AutenticarRequest authRequest = AutenticarRequest.builder().tituloEleitoral(tituloEleitoral)
                    .senha(senha).build();

            Cookie[] cookies = mockMvc.perform(post(BASE_URL + "/autenticar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true))
                    .andReturn().getResponse().getCookies();

            AutorizarRequest autorizarReq = AutorizarRequest.builder().tituloEleitoral(tituloEleitoral)
                    .build();

            mockMvc.perform(post(BASE_URL + "/autorizar")
                            .with(csrf())
                            .cookie(cookies)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(autorizarReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[*].perfil")
                            .value(containsInAnyOrder("ADMIN", "GESTOR")));

            // Entrar como GESTOR na unidadeGestor
            EntrarRequest entrarReq = EntrarRequest.builder()
                    .tituloEleitoral(tituloEleitoral)
                    .perfil("GESTOR")
                    .unidadeCodigo(unidadeGestor.getCodigo())
                    .build();
            mockMvc.perform(post(BASE_URL + "/entrar")
                            .with(csrf())
                            .cookie(cookies)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(entrarReq)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve falhar ao tentar autorizar com usuário não autenticado")
        void testAutorizar_falhaUsuarioNaoAutenticado() throws Exception {
            // Arrange
            String tituloEleitoral = "888888888888"; // Usuário inexistente

            // Não realizamos a autenticação prévia para simular um usuário não autenticado.

            // Act & Assert
            // Sem autenticação prévia (sessão válida), a tentativa de autorizar deve ser
            // rejeitada com 401 (Unauthorized)
            AutorizarRequest autorizarReq = AutorizarRequest.builder().tituloEleitoral(tituloEleitoral)
                    .build();

            mockMvc.perform(post(BASE_URL + "/autorizar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(autorizarReq)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Deve falhar ao tentar entrar com unidade inexistente")
        void testEntrar_falhaUnidadeInexistente() throws Exception {
            // Arrange
            String tituloEleitoral = usuarioAdmin.getTituloEleitoral();
            long codigoUnidadeInexistente = 999999L;

            // Pre-authenticate (Required by security fix)
            AutenticarRequest authRequest = AutenticarRequest.builder()
                    .tituloEleitoral(tituloEleitoral)
                    .senha("any")
                    .build();
            Cookie[] cookies = mockMvc.perform(post(BASE_URL + "/autenticar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(authRequest)))
                    .andReturn().getResponse().getCookies();

            EntrarRequest entrarReq = EntrarRequest.builder()
                    .tituloEleitoral(tituloEleitoral)
                    .perfil("ADMIN")
                    .unidadeCodigo(codigoUnidadeInexistente)
                    .build();

            // Act & Assert
            // A unidade inexistente retorna 404 (NOT_FOUND) via ErroEntidadeNaoEncontrada
            mockMvc.perform(post(BASE_URL + "/entrar")
                            .with(csrf())
                            .cookie(cookies)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testUtil.toJson(entrarReq)))
                    .andExpect(status().is(404))
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
