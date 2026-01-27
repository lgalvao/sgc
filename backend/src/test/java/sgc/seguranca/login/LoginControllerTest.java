package sgc.seguranca.login;

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
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.login.dto.AutenticarRequest;
import sgc.seguranca.login.dto.AutorizarRequest;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.seguranca.login.dto.PerfilUnidadeDto;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private sgc.organizacao.UsuarioFacade usuarioService;

    @MockitoBean
    private LimitadorTentativasLogin limitadorTentativasLogin;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        objectMapper = new ObjectMapper();
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

        mockMvc.perform(post("/api/usuarios/autenticar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(limitadorTentativasLogin).verificar(anyString());
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve obter IP do header X-Forwarded-For")
    @WithMockUser
    void autenticar_IpHeader() throws Exception {
        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();

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

        AutorizarRequest req = AutorizarRequest.builder().tituloEleitoral("123").build();

        mockMvc.perform(post("/api/usuarios/autorizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].perfil").value("ADMIN"));
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
        when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/entrar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.nome").value("Admin User"));
    }
}
