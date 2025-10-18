package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import sgc.integracao.mocks.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.sgrh.Perfil;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.sgrh.dto.AutenticacaoRequest;
import sgc.sgrh.dto.EntrarRequest;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;
import sgc.util.TestUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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

    @MockBean
    private SgrhService sgrhService;

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

            when(sgrhService.buscarPerfisUsuario(String.valueOf(tituloEleitoral)))
                .thenReturn(Collections.singletonList(
                    new PerfilDto(String.valueOf(tituloEleitoral), unidadeAdmin.getCodigo(), unidadeAdmin.getNome(), "ADMIN")
                ));

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

            when(sgrhService.buscarPerfisUsuario(String.valueOf(tituloEleitoral)))
                .thenReturn(List.of(
                    new PerfilDto(String.valueOf(tituloEleitoral), unidadeAdmin.getCodigo(), unidadeAdmin.getNome(), "ADMIN"),
                    new PerfilDto(String.valueOf(tituloEleitoral), unidadeGestor.getCodigo(), unidadeGestor.getNome(), "GESTOR")
                ));

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
                .andExpect(jsonPath("$[0].perfil").value("ADMIN"))
                .andExpect(jsonPath("$[1].perfil").value("GESTOR"));

            // Act & Assert: Etapa 3 - Entrar (Escolhendo o perfil GESTOR)
            EntrarRequest entrarRequest = EntrarRequest.builder().tituloEleitoral(tituloEleitoral).perfil("GESTOR").unidadeCodigo(unidadeGestor.getCodigo()).build();
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
            long codigoUnidadeInexistente = 999L;

            when(sgrhService.buscarPerfisUsuario(String.valueOf(tituloEleitoral)))
                .thenReturn(Collections.singletonList(
                    new PerfilDto(String.valueOf(tituloEleitoral), codigoUnidadeInexistente, "Unidade Fantasma", "ADMIN")
                ));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/autorizar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.valueOf(tituloEleitoral)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Unidade não encontrada com código: " + codigoUnidadeInexistente));
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