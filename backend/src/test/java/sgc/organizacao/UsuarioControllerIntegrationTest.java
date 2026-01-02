package sgc.organizacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sgc.comum.TestUtil;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.seguranca.dto.AutenticacaoReq;
import sgc.seguranca.dto.EntrarReq;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@DisplayName("Testes de Integração do SgrhController")
class UsuarioControllerIntegrationTest {

    private static final String API_URL = "/api/usuarios";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioService usuarioService;

    private Unidade unidade;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        unidade = unidadeRepo.findById(100L).orElseThrow();
    }

    @Test
    @DisplayName("Deve autenticar")
    void autenticar_deveRetornarTrue() throws Exception {
        AutenticacaoReq request =
                AutenticacaoReq.builder().tituloEleitoral("123456789101").senha("senha").build();

        mockMvc.perform(
                        post(API_URL + "/autenticar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Deve autorizar e retornar perfis")
    void autorizar_deveRetornarPerfis() throws Exception {
        long tituloEleitoral = 111111111111L;

        // Autentica primeiro (necessário para autorizar)
        usuarioService.autenticar(Long.toString(tituloEleitoral), "senha-qualquer");

        // When/Then
        mockMvc.perform(
                        post(API_URL + "/autorizar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(Long.toString(tituloEleitoral)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].perfil").value(Perfil.ADMIN.toString()))
                .andExpect(jsonPath("$[0].unidade.sigla").value("ADMIN-UNIT"));
    }

    @Test
    @DisplayName("Deve entrar")
    void entrar_deveRetornarOk() throws Exception {
        // Simula a etapa prévia de autenticação para popular o cache 'autenticacoesRecentes' no serviço,
        // que é verificado pelo método entrar() para prevenir bypass.
        usuarioService.autenticar("111111111111", "senha-qualquer");

        EntrarReq request =
                EntrarReq.builder()
                        .tituloEleitoral("111111111111")
                        .perfil(Perfil.ADMIN.toString())
                        .unidadeCodigo(unidade.getCodigo())
                        .build();

        mockMvc.perform(
                        post(API_URL + "/entrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.convertObjectToJsonBytes(request)))
                .andExpect(status().isOk());
    }
}
