package sgc.seguranca.login;

import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;
import sgc.seguranca.dto.*;
import tools.jackson.databind.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@Import(RestExceptionHandler.class)
@DisplayName("LoginController - Testes de Controller")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginController loginController;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginFacade loginFacade;

    @MockitoBean
    private UsuarioFacade usuarioFacade;

    @MockitoBean
    private LimitadorTentativasLogin limitadorTentativasLogin;

    @MockitoBean
    private GerenciadorJwt gerenciadorJwt;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Deve autenticar com sucesso e concluir sessão direta")
    @WithMockUser
    void login_SessaoDiretaComSucesso() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        UnidadeResumoDto unidadeDto = UnidadeResumoDto.builder().codigo(1L).nome("Adm").sigla("ADM").build();
        PerfilUnidadeDto perfilUnidade = new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto);
        Usuario usuario = new Usuario();
        usuario.setNome("Admin user");
        usuario.setTituloEleitoral("123");

        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        when(loginFacade.buscarAutorizacoesUsuario("123")).thenReturn(List.of(perfilUnidade));
        when(loginFacade.entrar(any(EntrarRequest.class), eq("123"), anyList())).thenReturn("token-jwt");
        when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.requerSelecaoPerfil").value(false))
                .andExpect(jsonPath("$.sessao.nome").value("Admin user"))
                .andExpect(jsonPath("$.sessao.permissoes.mostrarCriarProcesso").value(true))
                .andExpect(jsonPath("$.sessao.permissoes.mostrarMenuAdministradores").value(true))
                .andExpect(cookie().exists("jwtToken"))
                .andExpect(cookie().maxAge("SGC_PRE_AUTH", 0));

        verify(limitadorTentativasLogin).verificar(anyString());
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Deve exigir seleção quando houver múltiplos perfis")
    @WithMockUser
    void login_MultiplosPerfis() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        UnidadeResumoDto unidadeUm = UnidadeResumoDto.builder().codigo(1L).nome("Unidade 1").sigla("U1").build();
        UnidadeResumoDto unidadeDois = UnidadeResumoDto.builder().codigo(2L).nome("Unidade 2").sigla("U2").build();
        PerfilUnidadeDto primeiro = new PerfilUnidadeDto(Perfil.CHEFE, unidadeUm);
        PerfilUnidadeDto segundo = new PerfilUnidadeDto(Perfil.GESTOR, unidadeDois);

        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        when(loginFacade.buscarAutorizacoesUsuario("123")).thenReturn(List.of(primeiro, segundo));
        when(gerenciadorJwt.gerarTokenPreAuth("123")).thenReturn("token-pre-auth");

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.requerSelecaoPerfil").value(true))
                .andExpect(jsonPath("$.sessao").isEmpty())
                .andExpect(jsonPath("$.perfisUnidades[0].perfil").value("CHEFE"))
                .andExpect(cookie().value("SGC_PRE_AUTH", "token-pre-auth"));

        verify(loginFacade, never()).entrar(any(EntrarRequest.class), anyString(), anyList());
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Deve retornar 401 quando credenciais inválidas")
    @WithMockUser
    void login_CredenciaisInvalidas() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(false);

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("jwtToken"))
                .andExpect(cookie().doesNotExist("SGC_PRE_AUTH"));
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Deve usar RemoteAddr do request para limite de tentativas")
    @WithMockUser
    void login_IpRemoteAddr() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        UnidadeResumoDto unidadeDto = UnidadeResumoDto.builder().codigo(1L).nome("Adm").sigla("ADM").build();
        PerfilUnidadeDto perfilUnidade = new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto);
        Usuario usuario = new Usuario();
        usuario.setNome("Admin user");
        usuario.setTituloEleitoral("123");
        when(loginFacade.buscarAutorizacoesUsuario("123")).thenReturn(List.of(perfilUnidade));
        when(loginFacade.entrar(any(EntrarRequest.class), eq("123"), anyList())).thenReturn("token-jwt");
        when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.50");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(limitadorTentativasLogin).verificar("192.168.1.50");
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Deve falhar com erro interno se usuário autenticado não tiver perfis")
    @WithMockUser
    void login_SemPerfis_DeveFalharComErroInterno() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        when(loginFacade.buscarAutorizacoesUsuario("123")).thenReturn(List.of());

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"));
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Deve rejeitar título não numérico")
    @WithMockUser
    void login_DeveRejeitarTituloNaoNumerico() throws Exception {
        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral("12\nFake")
                .senha("senha")
                .build();

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/usuarios/entrar - Deve realizar login com cookie de pré-auth")
    @WithMockUser
    void entrar_Sucesso() throws Exception {
        EntrarRequest req = EntrarRequest.builder()
                .perfil("ADMIN")
                .unidadeCodigo(1L)
                .build();

        Usuario usuario = new Usuario();
        usuario.setNome("Admin user");
        usuario.setTituloEleitoral("123");

        when(loginFacade.entrar(any(EntrarRequest.class), eq("123"))).thenReturn("token-jwt");
        when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);
        when(gerenciadorJwt.validarTokenPreAuth("token-pre-auth")).thenReturn(Optional.of("123"));

        mockMvc.perform(post("/api/usuarios/entrar")
                        .with(csrf())
                        .cookie(new Cookie("SGC_PRE_AUTH", "token-pre-auth"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Admin user"))
                .andExpect(jsonPath("$.permissoes.mostrarMenuConfiguracoes").value(true))
                .andExpect(cookie().value("jwtToken", "token-jwt"))
                .andExpect(cookie().maxAge("SGC_PRE_AUTH", 0));
    }

    @Test
    @DisplayName("POST /api/usuarios/entrar - Deve falhar sem cookie")
    @WithMockUser
    void entrar_SemCookie_DeveFalhar() throws Exception {
        EntrarRequest req = EntrarRequest.builder()
                .perfil("ADMIN")
                .unidadeCodigo(1L)
                .build();

        mockMvc.perform(post("/api/usuarios/entrar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/usuarios/entrar - Deve falhar com token inválido")
    @WithMockUser
    void entrar_TokenInvalido_DeveFalhar() throws Exception {
        EntrarRequest req = EntrarRequest.builder()
                .perfil("ADMIN")
                .unidadeCodigo(1L)
                .build();
        when(gerenciadorJwt.validarTokenPreAuth("token-invalido")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/usuarios/entrar")
                        .with(csrf())
                        .cookie(new Cookie("SGC_PRE_AUTH", "token-invalido"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/usuarios/entrar - Deve falhar quando houver cookies sem pré-auth")
    @WithMockUser
    void entrar_SemCookiePreAuthMesmoComOutrosCookies_DeveFalhar() throws Exception {
        EntrarRequest req = EntrarRequest.builder()
                .perfil("ADMIN")
                .unidadeCodigo(1L)
                .build();

        mockMvc.perform(post("/api/usuarios/entrar")
                        .with(csrf())
                        .cookie(new Cookie("OUTRO", "valor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/usuarios/logout - Deve limpar cookies de autenticação")
    @WithMockUser
    void logout_DeveLimparCookiesAutenticacao() throws Exception {
        mockMvc.perform(post("/api/usuarios/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("jwtToken", 0))
                .andExpect(cookie().maxAge("SGC_PRE_AUTH", 0));
    }

    @Test
    @DisplayName("POST /api/usuarios/entrar - Deve aceitar pré-auth mesmo com cookies adicionais antes")
    @WithMockUser
    void entrar_ComCookiesAdicionaisAntesDoPreaAuth_DeveAutenticar() throws Exception {
        EntrarRequest req = EntrarRequest.builder()
                .perfil("ADMIN")
                .unidadeCodigo(1L)
                .build();

        Usuario usuario = new Usuario();
        usuario.setNome("Admin user");
        usuario.setTituloEleitoral("123");

        when(loginFacade.entrar(any(EntrarRequest.class), eq("123"))).thenReturn("token-jwt");
        when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);
        when(gerenciadorJwt.validarTokenPreAuth("token-pre-auth")).thenReturn(Optional.of("123"));

        mockMvc.perform(post("/api/usuarios/entrar")
                        .with(csrf())
                        .cookie(new Cookie("OUTRO", "x"), new Cookie("SGC_PRE_AUTH", "token-pre-auth"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Admin user"));
    }

    @Test
    @DisplayName("POST /api/usuarios/login - IP sanitizado (log injection) deve ser repassado ao limitador")
    @WithMockUser
    void login_IpComQuebraDeLinha_DevePassarIpSanitizadoAoLimitador() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        UnidadeResumoDto unidadeDto = UnidadeResumoDto.builder().codigo(1L).nome("Adm").sigla("ADM").build();
        when(loginFacade.buscarAutorizacoesUsuario("123"))
                .thenReturn(List.of(new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto)));
        when(loginFacade.entrar(any(EntrarRequest.class), eq("123"), anyList())).thenReturn("token-jwt");
        Usuario usuario = new Usuario();
        usuario.setNome("Admin user");
        usuario.setTituloEleitoral("123");
        when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.1\n\rFAKE");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(limitadorTentativasLogin).verificar("192.168.1.1__FAKE");
    }

    @Test
    @DisplayName("POST /api/usuarios/login - IP nulo não deve impedir login")
    @WithMockUser
    void login_IpNulo_DeveContinuarFluxoSemLimitador() throws Exception {
        AutenticarRequest req = criarRequestPadrao();
        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        UnidadeResumoDto unidadeDto = UnidadeResumoDto.builder().codigo(1L).nome("Adm").sigla("ADM").build();
        when(loginFacade.buscarAutorizacoesUsuario("123"))
                .thenReturn(List.of(new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto)));
        when(loginFacade.entrar(any(EntrarRequest.class), eq("123"), anyList())).thenReturn("token-jwt");
        Usuario usuario = new Usuario();
        usuario.setNome("Admin user");
        usuario.setTituloEleitoral("123");
        when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(null);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(limitadorTentativasLogin, never()).verificar(any());
    }

    @Test
    @DisplayName("login direto deve ignorar limitador quando o request não informar IP")
    void loginDiretoDeveIgnorarLimitadorQuandoORequestNaoInformarIp() {
        AutenticarRequest req = criarRequestPadrao();
        UnidadeResumoDto unidadeDto = UnidadeResumoDto.builder().codigo(1L).nome("Adm").sigla("ADM").build();
        PerfilUnidadeDto perfilUnidade = new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto);
        Usuario usuario = new Usuario();
        usuario.setNome("Admin user");
        usuario.setTituloEleitoral("123");

        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        when(loginFacade.buscarAutorizacoesUsuario("123")).thenReturn(List.of(perfilUnidade));
        when(loginFacade.entrar(any(EntrarRequest.class), eq("123"), anyList())).thenReturn("token-jwt");
        when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        when(httpRequest.getRemoteAddr()).thenReturn(null);

        var resposta = loginController.login(req, httpRequest, httpResponse);

        assertThat(resposta.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().autenticado()).isTrue();
        verify(limitadorTentativasLogin, never()).verificar(any());
    }

    @Test
    @DisplayName("login direto deve exigir seleção quando houver múltiplos perfis")
    void loginDiretoDeveExigirSelecaoQuandoHouverMultiplosPerfis() {
        AutenticarRequest req = criarRequestPadrao();
        PerfilUnidadeDto primeiro = new PerfilUnidadeDto(Perfil.CHEFE,
                UnidadeResumoDto.builder().codigo(1L).nome("Unidade 1").sigla("U1").build());
        PerfilUnidadeDto segundo = new PerfilUnidadeDto(Perfil.GESTOR,
                UnidadeResumoDto.builder().codigo(2L).nome("Unidade 2").sigla("U2").build());

        when(loginFacade.autenticar("123", "senha")).thenReturn(true);
        when(loginFacade.buscarAutorizacoesUsuario("123")).thenReturn(List.of(primeiro, segundo));
        when(gerenciadorJwt.gerarTokenPreAuth("123")).thenReturn("token-pre-auth");

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        when(httpRequest.getRemoteAddr()).thenReturn("10.0.0.1");

        var resposta = loginController.login(req, httpRequest, httpResponse);

        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().requerSelecaoPerfil()).isTrue();
        verify(limitadorTentativasLogin).verificar("10.0.0.1");
    }

    @Nested
    @DisplayName("Cookies sem flag Secure (aplicacao.cookies.secure=false)")
    class CookiesSemSecure {

        @Test
        @DisplayName("POST /api/usuarios/login - Cookies gerados não devem ter flag Secure quando desabilitado")
        @WithMockUser
        void login_CookiesNaoDevemSerSecureQuandoDesabilitado() throws Exception {
            AutenticarRequest req = criarRequestPadrao();
            UnidadeResumoDto unidadeDto = UnidadeResumoDto.builder().codigo(1L).nome("Adm").sigla("ADM").build();
            when(loginFacade.autenticar("123", "senha")).thenReturn(true);
            when(loginFacade.buscarAutorizacoesUsuario("123"))
                    .thenReturn(List.of(new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto)));
            when(loginFacade.entrar(any(EntrarRequest.class), eq("123"), anyList())).thenReturn("token-jwt");
            Usuario usuario = new Usuario();
            usuario.setNome("Admin user");
            usuario.setTituloEleitoral("123");
            when(usuarioFacade.buscarPorLogin("123")).thenReturn(usuario);

            mockMvc.perform(post("/api/usuarios/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("jwtToken"))
                    .andExpect(result -> {
                        Cookie jwt = result.getResponse().getCookie("jwtToken");
                        assertThat(jwt).isNotNull();
                        assertThat(jwt.getSecure()).isFalse();
                    });
        }
    }

    private AutenticarRequest criarRequestPadrao() {
        return AutenticarRequest.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();
    }
}
