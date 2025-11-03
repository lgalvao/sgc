package sgc.sgrh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.TestUtil;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.sgrh.dto.AutenticacaoRequest;
import sgc.sgrh.dto.EntrarRequest;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.modelo.Usuario;
import sgc.sgrh.modelo.UsuarioRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@DisplayName("Testes de Integração do UsuarioControle")
class UsuarioControleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private static final String API_URL = "/api/usuarios";
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.save(new Unidade("Teste", "TST"));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso")
    void autenticar_deveRetornarTrue() throws Exception {
        AutenticacaoRequest request = AutenticacaoRequest.builder()
            .tituloEleitoral(123456789101L)
            .senha("senha")
            .build();

        mockMvc.perform(post(API_URL + "/autenticar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));
    }

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Test
    @DisplayName("Deve autorizar e retornar perfis")
    void autorizar_deveRetornarPerfis() throws Exception {
        // Given
        Unidade unidadeSgp = unidadeRepo.save(new Unidade("Secretaria de Gestao de Pessoas", "SGP"));
        Long tituloEleitoral = 123456789101L;

        usuarioRepo.save(new Usuario(
            tituloEleitoral,
            "Usuário Teste",
            "teste@sgc.com",
            "123",
            unidadeSgp,
            Set.of(Perfil.CHEFE)
        ));

        // When/Then
        mockMvc.perform(post(API_URL + "/autorizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tituloEleitoral.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].perfil").value(Perfil.CHEFE.toString()))
            .andExpect(jsonPath("$[0].unidade.sigla").value("SGP"));
    }

    @Test
    @DisplayName("Deve entrar com sucesso")
    void entrar_deveRetornarOk() throws Exception {
        EntrarRequest request = EntrarRequest.builder()
            .tituloEleitoral(123456789101L)
            .perfil(Perfil.CHEFE.toString())
            .unidadeCodigo(unidade.getCodigo())
            .build();

        mockMvc.perform(post(API_URL + "/entrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(request)))
            .andExpect(status().isOk());
    }
}