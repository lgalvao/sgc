package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.sgrh.dto.AutenticacaoReq;
import sgc.sgrh.dto.EntrarReq;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;
import sgc.util.TestUtil;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("CDU-01: Realizar Login")
public class CDU01IntegrationTest extends BaseIntegrationTest {
    private static final String BASE_URL = "/api/usuarios";


    @Autowired
    private TestUtil testUtil;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Unidade unidadeAdmin;

    @BeforeEach
    void setUp() {
        unidadeAdmin = unidadeRepo.findById(100L).orElseThrow();
    }

    @Nested
    @DisplayName("Testes de fluxo de login completo")
    class FluxoLoginTests {
        @Test
        @DisplayName("Deve realizar login completo para usuário com um único perfil")
        void testLoginCompleto_sucessoUsuarioUnicoPerfil() throws Exception {
            String tituloEleitoral = "111111111111";
            String senha = "password";
            AutenticacaoReq authRequest = AutenticacaoReq.builder().tituloEleitoral(tituloEleitoral).senha(senha)
                    .build();

            mockMvc.perform(post(BASE_URL + "/autenticar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tituloEleitoral))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].perfil").value("ADMIN"))
                    .andExpect(jsonPath("$[0].siglaUnidade").value("ADMIN-UNIT"));

            // Act & Assert: Etapa 3 - Entrar
            EntrarReq entrarReq = EntrarReq.builder().tituloEleitoral(tituloEleitoral).perfil("ADMIN")
                    .unidadeCodigo(unidadeAdmin.getCodigo()).build();
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarReq)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve realizar login completo para usuário com múltiplos perfis")
        void testLoginCompleto_sucessoUsuarioMultiplosPerfis() throws Exception {
            String tituloEleitoral = "999999999999"; // Usuario Multi Perfil from data-postgresql.sql (has ADMIN and
                                                     // GESTOR)
            String senha = "password";
            AutenticacaoReq authRequest = AutenticacaoReq.builder().tituloEleitoral(tituloEleitoral).senha(senha)
                    .build();

            mockMvc.perform(post(BASE_URL + "/autenticar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tituloEleitoral))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[*].perfil").value(containsInAnyOrder("ADMIN", "GESTOR")));

            EntrarReq entrarReq = EntrarReq.builder().tituloEleitoral(tituloEleitoral).perfil("GESTOR")
                    .unidadeCodigo(unidadeAdmin.getCodigo()).build();
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarReq)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve falhar ao tentar autorizar com unidade inexistente retornada pelo SGRH")
        void testAutorizar_falhaUnidadeInexistente() throws Exception {
            // Arrange
            String tituloEleitoral = "888888888888"; // Non-existent user

            // O SgrhService não é mais mockado. A lógica agora busca um usuário no banco.
            // Se o usuário não existe, a exceção será lançada.
            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tituloEleitoral))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            "&#39;Usuário&#39; com codigo &#39;" + tituloEleitoral + "&#39; não encontrado(a)."));
        }

        @Test
        @DisplayName("Deve falhar ao tentar entrar com unidade inexistente")
        void testEntrar_falhaUnidadeInexistente() throws Exception {
            // Arrange
            String tituloEleitoral = "111222333444";
            long codigoUnidadeInexistente = 999L;
            EntrarReq entrarReq = EntrarReq.builder().tituloEleitoral(tituloEleitoral).perfil("ADMIN")
                    .unidadeCodigo(codigoUnidadeInexistente).build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarReq)))
                    .andExpect(status().isNotFound())
                    .andExpect(
                            jsonPath("$.message").value("Unidade não encontrada, código: " + codigoUnidadeInexistente));
        }
    }
}