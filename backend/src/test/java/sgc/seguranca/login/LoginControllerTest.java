package sgc.seguranca.login;

import jakarta.servlet.http.Cookie;
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
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.login.dto.AutenticarRequest;
import sgc.seguranca.login.dto.AutorizarRequest;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.seguranca.login.dto.PerfilUnidadeDto;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAutenticacao;

@WebMvcTest(LoginController.class)
@Import(RestExceptionHandler.class)
@DisplayName("LoginController - Testes de Controller")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginFacade loginFacade;

    @MockitoBean
    private OrganizacaoFacade organizacaoFacade;

    @MockitoBean
    private LimitadorTentativasLogin limitadorTentativasLogin;

    @MockitoBean
    private GerenciadorJwt gerenciadorJwt;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/usuarios/autorizar - Deve falhar com cookies presentes mas sem pre-auth")
    @WithMockUser
    void autorizar_CookiesSemPreAuth_DeveFalhar() throws Exception {
        AutorizarRequest req = AutorizarRequest.builder().tituloEleitoral("123").build();

        mockMvc.perform(post("/api/usuarios/autorizar")
                        .with(csrf())
                        .cookie(new Cookie("OUTRO_COOKIE", "valor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve autenticar com sucesso")
    @WithMockUser
    void autenticar_Sucesso() throws Exception {
        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();

        doNothing().when(limitadorTentativasLogin).verificar(anyString());
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        when(gerenciadorJwt.gerarTokenPreAuth("123")).thenReturn("token-pre-auth");

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andExpect(cookie().exists("SGC_PRE_AUTH"))
                .andExpect(cookie().httpOnly("SGC_PRE_AUTH", true));

        verify(limitadorTentativasLogin).verificar(anyString());
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve retornar false quando credenciais inválidas")
    @WithMockUser
    void autenticar_FalhaAutenticacao() throws Exception {
        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral("123")
                .senha("senhaErrada")
                .build();

        doNothing().when(limitadorTentativasLogin).verificar(anyString());
        when(loginFacade.autenticar("123", "senhaErrada")).thenReturn(false);

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andExpect(cookie().doesNotExist("SGC_PRE_AUTH"));

        verify(limitadorTentativasLogin).verificar(anyString());
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve obter IP do header X-Forwarded-For")
    @WithMockUser
    void autenticar_IpHeader() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .header("X-Forwarded-For", "10.0.0.1, 10.0.0.2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(limitadorTentativasLogin).verificar("10.0.0.1");
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve usar RemoteAddr quando X-Forwarded-For for nulo")
    @WithMockUser
    void autenticar_IpRemoteAddrQuandoHeaderNull() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(limitadorTentativasLogin).verificar("192.168.1.1");
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve usar RemoteAddr quando X-Forwarded-For for vazio")
    @WithMockUser
    void autenticar_IpRemoteAddrQuandoHeaderVazio() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .header("X-Forwarded-For", "")
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(limitadorTentativasLogin).verificar("192.168.1.1");
    }

    private AutenticarRequest criarRequestPadrao() {
        return AutenticarRequest.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve rejeitar título não numérico")
    @WithMockUser
    void autenticar_DeveRejeitarTituloNaoNumerico() throws Exception {
        // Log Injection Payload: Digits followed by newline and fake log
        // Must be <= 12 chars to bypass @Size check, but contains newline/letters to fail @Pattern
        String maliciousTitle = "12\nFake";

        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral(maliciousTitle)
                .senha("senha")
                .build();

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/usuarios/autorizar - Deve retornar perfis")
    @WithMockUser
    void autorizar_Sucesso() throws Exception {
        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(1L).nome("AdmUnit").sigla("ADM").build();
        PerfilUnidadeDto pu = new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto);
        when(loginFacade.autorizar("123")).thenReturn(List.of(pu));
        when(gerenciadorJwt.validarTokenPreAuth("token-pre-auth")).thenReturn(Optional.of("123"));

        AutorizarRequest req = AutorizarRequest.builder().tituloEleitoral("123").build();

        mockMvc.perform(post("/api/usuarios/autorizar")
                        .with(csrf())
                        .cookie(new Cookie("SGC_PRE_AUTH", "token-pre-auth"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].perfil").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/usuarios/autorizar - Deve retornar lista vazia quando usuário sem perfis ativos")
    @WithMockUser
    void autorizar_DeveRetornarListaVaziaQuandoSemPerfisAtivos() throws Exception {
        when(loginFacade.autorizar("123")).thenReturn(List.of());
        when(gerenciadorJwt.validarTokenPreAuth("token-pre-auth")).thenReturn(Optional.of("123"));

        AutorizarRequest req = AutorizarRequest.builder().tituloEleitoral("123").build();

        mockMvc.perform(post("/api/usuarios/autorizar")
                        .with(csrf())
                        .cookie(new Cookie("SGC_PRE_AUTH", "token-pre-auth"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/usuarios/entrar - Deve realizar login")
    @WithMockUser
    void entrar_Sucesso() throws Exception {
        EntrarRequest req = EntrarRequest.builder()
                .tituloEleitoral("123")
                .perfil("ADMIN")
                .unidadeCodigo(1L)
                .build();

        Usuario usuario = new Usuario();
        usuario.setNome("Admin User");
        usuario.setTituloEleitoral("123");

        when(loginFacade.entrar(any(EntrarRequest.class))).thenReturn("token-jwt");
        when(organizacaoFacade.buscarPorLogin("123")).thenReturn(usuario);
        when(gerenciadorJwt.validarTokenPreAuth("token-pre-auth")).thenReturn(Optional.of("123"));

        mockMvc.perform(post("/api/usuarios/entrar")
                        .with(csrf())
                        .cookie(new Cookie("SGC_PRE_AUTH", "token-pre-auth"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.nome").value("Admin User"));
    }

    @Test
    @DisplayName("POST /api/usuarios/autorizar - Deve falhar sem cookie")
    @WithMockUser
    void autorizar_SemCookie_DeveFalhar() throws Exception {
        AutorizarRequest req = AutorizarRequest.builder().tituloEleitoral("123").build();

        mockMvc.perform(post("/api/usuarios/autorizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/usuarios/autorizar - Deve falhar com token invalido")
    @WithMockUser
    void autorizar_TokenInvalido_DeveFalhar() throws Exception {
        AutorizarRequest req = AutorizarRequest.builder().tituloEleitoral("123").build();
        when(gerenciadorJwt.validarTokenPreAuth("token-invalido")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/usuarios/autorizar")
                        .with(csrf())
                        .cookie(new Cookie("SGC_PRE_AUTH", "token-invalido"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/usuarios/autorizar - Deve falhar se token nao corresponder ao usuario")
    @WithMockUser
    void autorizar_TokenOutroUsuario_DeveFalhar() throws Exception {
        AutorizarRequest req = AutorizarRequest.builder().tituloEleitoral("123").build();
        when(gerenciadorJwt.validarTokenPreAuth("token-outro")).thenReturn(Optional.of("456"));

        mockMvc.perform(post("/api/usuarios/autorizar")
                        .with(csrf())
                        .cookie(new Cookie("SGC_PRE_AUTH", "token-outro"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
    @Nested
    @DisplayName("Unit Tests (Isolated)")
    class UnitTests {

        private LoginController controller;
        private LoginFacade loginFacadeMock;
        private OrganizacaoFacade organizacaoFacadeMock;
        private LimitadorTentativasLogin limitadorMock;
        private GerenciadorJwt gerenciadorJwtMock;

        @BeforeEach
        void setUp() {
            loginFacadeMock = mock(LoginFacade.class);
            organizacaoFacadeMock = mock(OrganizacaoFacade.class);
            limitadorMock = mock(LimitadorTentativasLogin.class);
            gerenciadorJwtMock = mock(GerenciadorJwt.class);

            controller = new LoginController(
                loginFacadeMock, 
                organizacaoFacadeMock, 
                limitadorMock, 
                gerenciadorJwtMock
            );
        }

        @Test
        @DisplayName("autenticar deve configurar cookie seguro se ambienteTestes false")
        void autenticar_SecureCookie() {
            ReflectionTestUtils.setField(controller, "ambienteTestes", false);
            AutenticarRequest req = new AutenticarRequest("user", "pass");
            HttpServletRequest httpReq = mock(HttpServletRequest.class);
            HttpServletResponse httpRes = mock(HttpServletResponse.class);

            when(loginFacadeMock.autenticar("user", "pass")).thenReturn(true);
            when(gerenciadorJwtMock.gerarTokenPreAuth("user")).thenReturn("token");

            controller.autenticar(req, httpReq, httpRes);

            verify(httpRes).addCookie(argThat(Cookie::getSecure));
        }

        @Test
        @DisplayName("autenticar NAO deve configurar cookie seguro se ambienteTestes true")
        void autenticar_NonSecureCookie() {
            ReflectionTestUtils.setField(controller, "ambienteTestes", true);
            AutenticarRequest req = new AutenticarRequest("user", "pass");
            HttpServletRequest httpReq = mock(HttpServletRequest.class);
            HttpServletResponse httpRes = mock(HttpServletResponse.class);

            when(loginFacadeMock.autenticar("user", "pass")).thenReturn(true);
            when(gerenciadorJwtMock.gerarTokenPreAuth("user")).thenReturn("token");

            controller.autenticar(req, httpReq, httpRes);

            verify(httpRes).addCookie(argThat(cookie -> !cookie.getSecure()));
        }

        @Test
        @DisplayName("entrar deve lidar com cookie invalido e lancar ErroAutenticacao")
        void entrar_CookieInvalido() {
            EntrarRequest req = new EntrarRequest("user", "ADMIN", 1L);
            HttpServletRequest httpReq = mock(HttpServletRequest.class);
            
            // No cookies
            when(httpReq.getCookies()).thenReturn(null);

            Assertions.assertThrows(ErroAutenticacao.class, 
                () -> controller.entrar(req, httpReq));
        }

        @Test
        @DisplayName("entrar deve lidar com sessao invalida (token mismatch)")
        void entrar_TokenMismatch() {
            EntrarRequest req = new EntrarRequest("user", "ADMIN", 1L);
            HttpServletRequest httpReq = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("SGC_PRE_AUTH", "token");
            
            when(httpReq.getCookies()).thenReturn(new Cookie[]{cookie});
            when(gerenciadorJwtMock.validarTokenPreAuth("token")).thenReturn(Optional.of("otherUser"));

            Assertions.assertThrows(ErroAutenticacao.class, 
                () -> controller.entrar(req, httpReq));
        }
        
        @Test
        @DisplayName("entrar deve lidar com sessao invalida (token empty)")
        void entrar_TokenEmpty() {
            EntrarRequest req = new EntrarRequest("user", "ADMIN", 1L);
            HttpServletRequest httpReq = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("SGC_PRE_AUTH", "token");
            
            when(httpReq.getCookies()).thenReturn(new Cookie[]{cookie});
            when(gerenciadorJwtMock.validarTokenPreAuth("token")).thenReturn(Optional.empty());

            Assertions.assertThrows(ErroAutenticacao.class, 
                () -> controller.entrar(req, httpReq));
        }
    }
}
