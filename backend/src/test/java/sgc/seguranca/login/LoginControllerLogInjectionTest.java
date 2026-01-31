package sgc.seguranca.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.UsuarioFacade;
import sgc.seguranca.login.dto.AutenticarRequest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@Import(RestExceptionHandler.class)
@DisplayName("LoginController - Teste de Log Injection")
class LoginControllerLogInjectionTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginFacade loginFacade;

    @MockitoBean
    private UsuarioFacade usuarioService;

    @MockitoBean
    private LimitadorTentativasLogin limitadorTentativasLogin;

    @MockitoBean
    private GerenciadorJwt gerenciadorJwt;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve sanitizar IP no header X-Forwarded-For (Log Injection)")
    @WithMockUser
    void autenticar_DeveSanitizarIpLogInjection() throws Exception {
        // Payload de Log Injection: Tenta injetar uma nova linha e um log falso
        // O valor original poderia ser algo como: "10.0.0.1\nINFO User logged in as admin"
        String ipMalicioso = "10.0.0.1\nINFO User logged in as admin";

        // O valor sanitizado deve ter a nova linha removida ou substituída
        // Vamos assumir que substituímos por sublinhado ou removemos.

        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();

        when(loginFacade.autenticar("123", "senha")).thenReturn(true);

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .header("X-Forwarded-For", ipMalicioso)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Verifica se o valor passado para o limitador (e consequentemente para o log) foi sanitizado.
        // Esperamos que não contenha \n ou \r
        verify(limitadorTentativasLogin).verificar("10.0.0.1_INFO User logged in as admin");
    }
}
