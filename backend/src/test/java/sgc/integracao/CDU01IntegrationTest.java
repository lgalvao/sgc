package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import sgc.integracao.mocks.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.sgrh.dto.AutenticacaoRequest;
import sgc.sgrh.dto.EntrarRequest;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;
import sgc.util.TestUtil;

import java.util.Collections;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("CDU-01: Realizar Login")
public class CDU01IntegrationTest {

    private static final String BASE_URL = "/api/usuarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtil testUtil;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    private Unidade unidadeAdmin;
    private Unidade unidadeGestor;

    @BeforeEach
    void setUp() {
        unidadeAdmin = unidadeRepo.save(new Unidade("Unidade Admin", "ADM"));
        unidadeGestor = unidadeRepo.save(new Unidade("Unidade Gestor", "GST"));
    }

    @Nested
    @DisplayName("Testes de fluxo de login completo")
    class FluxoLoginTests {

        @Test
        @DisplayName("Deve realizar login completo com sucesso para usuário com um único perfil")
        void testLoginCompleto_sucessoUsuarioUnicoPerfil() throws Exception {
            // Arrange
            long tituloEleitoral = 123456789012L;
            String senha = "password";
            AutenticacaoRequest authRequest = AutenticacaoRequest.builder().tituloEleitoral(tituloEleitoral).senha(senha).build();

            usuarioRepo.save(new Usuario(tituloEleitoral, "Usuário Admin", "admin@email.com", "123", unidadeAdmin, Collections.singleton(Perfil.ADMIN)));

            // Act & Assert: Etapa 1 - Autenticar
            mockMvc.perform(post(BASE_URL + "/autenticar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

            // Act & Assert: Etapa 2 - Autorizar
            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.valueOf(tituloEleitoral)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].perfil").value("ADMIN"))
                .andExpect(jsonPath("$[0].siglaUnidade").value("ADM"));

            // Act & Assert: Etapa 3 - Entrar
            EntrarRequest entrarRequest = EntrarRequest.builder().tituloEleitoral(tituloEleitoral).perfil("ADMIN").unidadeCodigo(unidadeAdmin.getCodigo()).build();
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve realizar login completo com sucesso para usuário com múltiplos perfis")
        void testLoginCompleto_sucessoUsuarioMultiplosPerfis() throws Exception {
            // Arrange
            long tituloEleitoral = 987654321098L;
            String senha = "password";
            AutenticacaoRequest authRequest = AutenticacaoRequest.builder().tituloEleitoral(tituloEleitoral).senha(senha).build();

            usuarioRepo.save(new Usuario(tituloEleitoral, "Usuário Múltiplo", "multiplo@email.com", "456", unidadeAdmin, Set.of(Perfil.ADMIN, Perfil.GESTOR)));
            // Assumindo que o usuário está associado primariamente a uma unidade (unidadeAdmin),
            // mas tem perfil em outra (unidadeGestor). O modelo atual simplifica isso,
            // associando o usuário a uma única unidade principal.

            // Act & Assert: Etapa 1 - Autenticar
            mockMvc.perform(post(BASE_URL + "/autenticar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

            // Act & Assert: Etapa 2 - Autorizar
            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.valueOf(tituloEleitoral)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].perfil").value(org.hamcrest.Matchers.containsInAnyOrder("ADMIN", "GESTOR")));

            // Act & Assert: Etapa 3 - Entrar (Escolhendo o perfil GESTOR)
            EntrarRequest entrarRequest = EntrarRequest.builder().tituloEleitoral(tituloEleitoral).perfil("GESTOR").unidadeCodigo(unidadeAdmin.getCodigo()).build();
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve falhar ao tentar autorizar com unidade inexistente retornada pelo SGRH")
        void testAutorizar_falhaUnidadeInexistente() throws Exception {
            // Arrange
            long tituloEleitoral = 111222333444L;

            // O SgrhService não é mais mockado. A lógica agora busca um usuário no banco.
            // Se o usuário não existe, a exceção será lançada.
            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.valueOf(tituloEleitoral)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("&#39;Usuário&#39; com id &#39;" + tituloEleitoral + "&#39; não encontrado(a)."));
        }

        @Test
        @DisplayName("Deve falhar ao tentar entrar com unidade inexistente")
        void testEntrar_falhaUnidadeInexistente() throws Exception {
            // Arrange
            long tituloEleitoral = 111222333444L;
            long codigoUnidadeInexistente = 999L;
            EntrarRequest entrarRequest = EntrarRequest.builder().tituloEleitoral(tituloEleitoral).perfil("ADMIN").unidadeCodigo(codigoUnidadeInexistente).build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Unidade não encontrada com código: " + codigoUnidadeInexistente));
        }
    }
}