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
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.dto.UnidadeResumoDto;
import sgc.organizacao.model.Perfil;
import sgc.seguranca.LoginAplicacaoService;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.seguranca.dto.AutenticarRequest;
import sgc.seguranca.dto.PerfilUnidadeDto;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@Import(RestExceptionHandler.class)
@DisplayName("LoginController - Teste de Log injection")
class LoginControllerLogInjectionTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginAplicacaoService loginAplicacaoService;

    @MockitoBean
    private UsuarioAplicacaoService usuarioAplicacaoService;

    @MockitoBean
    private LimitadorTentativasLogin limitadorTentativasLogin;

    @MockitoBean
    private GerenciadorJwt gerenciadorJwt;
    @MockitoBean
    private ListaNegraJwt listaNegraJwt;
    @MockitoBean
    private sgc.seguranca.config.JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Deve sanitizar IP no header X-Forwarded-For (Log injection)")
    @WithMockUser
    void autenticar_DeveSanitizarIpLogInjection() throws Exception {
        // Payload de Log injection: Tenta injetar uma nova linha e um log falso
        // O valor original poderia ser algo como: "10.0.0.1\nINFO User logged in as admin"
        String ipMalicioso = "10.0.0.1\nINFO User logged in as admin";

        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();

        when(loginAplicacaoService.autenticar("123", "senha")).thenReturn(true);
        when(loginAplicacaoService.buscarAutorizacoesUsuario("123")).thenReturn(java.util.List.of(
                new PerfilUnidadeDto(Perfil.CHEFE.name(), UnidadeResumoDto.builder().codigo(1L).sigla("U1").nome("Unidade 1").build()),
                new PerfilUnidadeDto(Perfil.GESTOR.name(), UnidadeResumoDto.builder().codigo(2L).sigla("U2").nome("Unidade 2").build())
        ));
        when(gerenciadorJwt.gerarTokenPreAuth("123")).thenReturn("token-pre-auth");

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(ipMalicioso);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Esperamos que não contenha \n ou \r
        verify(limitadorTentativasLogin).verificar("10.0.0.1_INFO User logged in as admin");
    }
}
