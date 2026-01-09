package sgc.organizacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.seguranca.autenticacao.AutenticarReq;
import sgc.seguranca.dto.EntrarReq;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
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
        // Usa um usuário existente no data.sql (Admin Teste)
        AutenticarReq request =
                AutenticarReq.builder().tituloEleitoral("111111111111").senha("senha").build();

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
        // Nota: getTodasAtribuicoes() usa HashSet, então a ordem não é garantida
        // O teste verifica que os perfis existem, mas não a ordem específica
        mockMvc.perform(
                        post(API_URL + "/autorizar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(Long.toString(tituloEleitoral)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.perfil=='ADMIN' && @.unidade.codigo==100)]").exists())
                .andExpect(jsonPath("$[?(@.perfil=='CHEFE' && @.unidade.codigo==102)]").exists());
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
