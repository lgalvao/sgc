package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-02: Visualizar Painel")
@Import(TestSecurityConfig.class)
class CDU02IntegrationTest extends BaseIntegrationTest {

    private static final String API_PAINEL_PROCESSOS = "/api/painel/processos";
    private static final String API_PAINEL_ALERTAS = "/api/painel/alertas";

    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    // Unidades
    private Unidade unidadeRaiz;
    private Unidade unidadeFilha1;
    private Unidade unidadeFilha2;

    @BeforeEach
    void setup() {
        unidadeRaiz = unidadeRepo.findById(2L).orElseThrow();
        unidadeFilha1 = unidadeRepo.findById(6L).orElseThrow();
        unidadeFilha2 = unidadeRepo.findById(7L).orElseThrow();
    }

    private void setupSecurityContext(String tituloEleitoral, Unidade unidade, String... perfis) {
        Usuario principal =
                usuarioRepo
                        .findById(tituloEleitoral)
                        .orElseGet(
                                () -> {
                                    Usuario newUser =
                                            new Usuario(
                                                    tituloEleitoral,
                                                    "Usuario de Teste",
                                                    "teste@sgc.com",
                                                    "123",
                                                    unidade);
                                    Usuario savedUser = usuarioRepo.save(newUser);
                                    for (String perfilStr : perfis) {
                                        // Insert na VIEW_USUARIO_PERFIL_UNIDADE
                                        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                                                savedUser.getTituloEleitoral(), unidade.getCodigo(), perfilStr);
                                    }
                                    return savedUser;
                                });

        // Manually populate cache for the principal object because entity doesn't fetch from View automatically
        // and we need getAuthorities() to work for Authentication, and AlertaMapper uses getTodasAtribuicoes().
        java.util.Set<sgc.sgrh.model.UsuarioPerfil> perfisSet = new java.util.HashSet<>();
        for (String perfilStr : perfis) {
            perfisSet.add(sgc.sgrh.model.UsuarioPerfil.builder()
                    .usuario(principal)
                    .unidade(unidade)
                    .perfil(Perfil.valueOf(perfilStr))
                    .build());
        }
        principal.setAtribuicoes(perfisSet);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Processos")
    class VisibilidadeProcessosTestes {

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve ver todos os processos, incluindo os com status 'Criado'")
        void testListarProcessos_Admin_VeTodos() throws Exception {
            mockMvc.perform(get(API_PAINEL_PROCESSOS).param("perfil", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(4))));
        }

        @Test
        @DisplayName(
                "GESTOR da unidade raiz deve ver todos os processos da sua unidade e de todas as"
                        + " subordinadas")
        void testListarProcessos_GestorRaiz_VeTodos() throws Exception {
            setupSecurityContext("1", unidadeRaiz, "GESTOR");
            mockMvc.perform(
                            get(API_PAINEL_PROCESSOS)
                                    .param("perfil", "GESTOR")
                                    .param("unidade", unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))));
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 1 deve ver processos da sua unidade e da Neta 1")
        void testListarProcessos_ChefeUnidadeFilha1_VeProcessosSubordinados() throws Exception {
            setupSecurityContext("2", unidadeFilha1, "CHEFE");
            mockMvc.perform(
                            get(API_PAINEL_PROCESSOS)
                                    .param("perfil", "CHEFE")
                                    .param("unidade", unidadeFilha1.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 2 não deve ver processos de outras unidades")
        void testListarProcessos_ChefeUnidadeFilha2_NaoVeProcessosDeOutros() throws Exception {
            setupSecurityContext("3", unidadeFilha2, "CHEFE");
            mockMvc.perform(
                            get(API_PAINEL_PROCESSOS)
                                    .param("perfil", "CHEFE")
                                    .param("unidade", unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("Nenhum perfil, exceto ADMIN, deve ver processos com status 'Criado'")
        void testListarProcessos_NaoAdmin_NaoVeProcessosCriados() throws Exception {
            setupSecurityContext("1", unidadeRaiz, "GESTOR");
            mockMvc.perform(
                            get(API_PAINEL_PROCESSOS)
                                    .param("perfil", "GESTOR")
                                    .param("unidade", unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.content[?(@.descricao == 'Processo Criado')]", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Alertas")
    class VisibilidadeAlertasTestes {

        @Test
        @DisplayName("Usuário deve ver alertas direcionados a ele")
        void testListarAlertas_UsuarioVeSeusAlertas() throws Exception {
            setupSecurityContext("8", unidadeRaiz, "GESTOR");
            mockMvc.perform(get(API_PAINEL_ALERTAS).param("usuarioTitulo", "8"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].descricao").value("Alerta para Gestor"));
        }

        @Test
        @DisplayName("Usuário deve ver alertas direcionados à sua unidade e às suas subordinadas")
        void testListarAlertas_UsuarioVeAlertasDaSuaUnidade() throws Exception {
            setupSecurityContext("2", unidadeFilha1, "CHEFE");
            mockMvc.perform(
                            get(API_PAINEL_ALERTAS)
                                    .param("codigoUnidade", unidadeFilha1.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(
                            jsonPath("$.content[?(@.descricao == 'Alerta para Unidade Filha 1')]")
                                    .exists());
        }

        @Test
        @DisplayName("Usuário não deve ver alertas de outros usuários ou unidades")
        void testListarAlertas_UsuarioNaoVeAlertasDeOutros() throws Exception {
            setupSecurityContext("3", unidadeFilha2, "CHEFE");
            mockMvc.perform(
                            get(API_PAINEL_ALERTAS)
                                    .param("usuarioTitulo", "3")
                                    .param("codigoUnidade", unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }
}
