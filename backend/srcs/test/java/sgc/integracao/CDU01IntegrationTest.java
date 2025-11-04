package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.sgrh.dto.AutenticacaoRequest;
import sgc.sgrh.dto.EntrarRequest;
import sgc.util.TestUtil;

import static org.hamcrest.Matchers.containsInAnyOrder;
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

    @Nested
    @DisplayName("Testes de fluxo de login completo")
    class FluxoLoginTests {
        @Test
        @DisplayName("Deve realizar login completo com sucesso para usuário com um único perfil")
        void testLoginCompleto_sucessoUsuarioUnicoPerfil() throws Exception {
            long tituloEleitoral = 111111111111L;
            long unidadeCodigo = 100L;
            String senha = "password";
            AutenticacaoRequest authRequest = AutenticacaoRequest.builder().tituloEleitoral(tituloEleitoral).senha(senha).build();

            mockMvc.perform(post(BASE_URL + "/autenticar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.valueOf(tituloEleitoral)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].perfil").value("ADMIN"))
                .andExpect(jsonPath("$[0].siglaUnidade").value("ADMIN-UNIT"));

            EntrarRequest entrarRequest = EntrarRequest.builder().tituloEleitoral(tituloEleitoral).perfil("ADMIN").unidadeCodigo(unidadeCodigo).build();
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve realizar login completo com sucesso para usuário com múltiplos perfis")
        void testLoginCompleto_sucessoUsuarioMultiplosPerfis() throws Exception {
            long tituloEleitoral = 999999999999L;
            long unidadeCodigo = 2L;
            String senha = "password";
            AutenticacaoRequest authRequest = AutenticacaoRequest.builder().tituloEleitoral(tituloEleitoral).senha(senha).build();

            mockMvc.perform(post(BASE_URL + "/autenticar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.valueOf(tituloEleitoral)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].perfil").value(containsInAnyOrder("ADMIN", "GESTOR")));

            EntrarRequest entrarRequest = EntrarRequest.builder().tituloEleitoral(tituloEleitoral).perfil("GESTOR").unidadeCodigo(unidadeCodigo).build();
            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve falhar ao tentar autorizar com usuário inexistente")
        void testAutorizar_falhaUsuarioInexistente() throws Exception {
            long tituloEleitoral = 123456789000L; // número que não existe no import.sql

            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.valueOf(tituloEleitoral)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve falhar ao tentar entrar com unidade inexistente")
        void testEntrar_falhaUnidadeInexistente() throws Exception {
            long tituloEleitoral = 111111111111L; // Usuário que existe
            long codigoUnidadeInexistente = 9999L; // Unidade que não existe

            EntrarRequest entrarRequest = EntrarRequest.builder().tituloEleitoral(tituloEleitoral).perfil("ADMIN").unidadeCodigo(codigoUnidadeInexistente).build();

            mockMvc.perform(post(BASE_URL + "/entrar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(entrarRequest)))
                .andExpect(status().isNotFound());
        }
    }
}
