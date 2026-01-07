package sgc.organizacao;

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
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.LimitadorTentativasLogin;
import sgc.seguranca.autenticacao.AutenticarReq;
import sgc.seguranca.dto.EntrarReq;
import sgc.seguranca.dto.PerfilUnidadeDto;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(RestExceptionHandler.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private LimitadorTentativasLogin limitadorTentativasLogin;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/usuarios/{titulo} - Deve retornar usuário quando encontrado")
    @WithMockUser
    void buscarUsuarioPorTitulo_Sucesso() throws Exception {
        UsuarioDto dto = UsuarioDto.builder().tituloEleitoral("123").nome("Teste").build();
        when(usuarioService.buscarUsuarioPorTitulo("123")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/usuarios/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloEleitoral").value("123"))
                .andExpect(jsonPath("$.nome").value("Teste"));
    }

    @Test
    @DisplayName("GET /api/usuarios/{titulo} - Deve retornar 404 quando não encontrado")
    @WithMockUser
    void buscarUsuarioPorTitulo_NaoEncontrado() throws Exception {
        when(usuarioService.buscarUsuarioPorTitulo("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/usuarios/autenticar - Deve autenticar com sucesso")
    @WithMockUser
    void autenticar_Sucesso() throws Exception {
        AutenticarReq req = AutenticarReq.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();

        doNothing().when(limitadorTentativasLogin).verificar(anyString());
        when(usuarioService.autenticar("123", "senha")).thenReturn(true);

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
        AutenticarReq req = AutenticarReq.builder()
                .tituloEleitoral("123")
                .senha("senha")
                .build();

        when(usuarioService.autenticar("123", "senha")).thenReturn(true);

        mockMvc.perform(post("/api/usuarios/autenticar")
                .with(csrf())
                .header("X-Forwarded-For", "10.0.0.1, 10.0.0.2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        
        // Verifica se usou o primeiro IP
        verify(limitadorTentativasLogin).verificar("10.0.0.1");
    }

    @Test
    @DisplayName("POST /api/usuarios/autorizar - Deve retornar perfis")
    @WithMockUser
    void autorizar_Sucesso() throws Exception {
        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(1L).nome("AdmUnit").sigla("ADM").build();
        PerfilUnidadeDto pu = new PerfilUnidadeDto(Perfil.ADMIN, unidadeDto);
        when(usuarioService.autorizar("123")).thenReturn(List.of(pu));

        mockMvc.perform(post("/api/usuarios/autorizar")
                .with(csrf())
                .content("123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].perfil").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/usuarios/entrar - Deve realizar login")
    @WithMockUser
    void entrar_Sucesso() throws Exception {
        EntrarReq req = EntrarReq.builder()
                .tituloEleitoral("123")
                .perfil("ADMIN")
                .unidadeCodigo(1L)
                .build();

        Usuario usuario = new Usuario();
        usuario.setNome("Admin User");
        
        when(usuarioService.entrar(any(EntrarReq.class))).thenReturn("token-jwt");
        when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/entrar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.nome").value("Admin User"));
    }

    @Test
    @DisplayName("GET /api/usuarios/administradores - Deve listar (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void listarAdministradores_Sucesso() throws Exception {
        AdministradorDto adm = AdministradorDto.builder().nome("Admin").build();
        when(usuarioService.listarAdministradores()).thenReturn(List.of(adm));

        mockMvc.perform(get("/api/usuarios/administradores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Admin"));
    }

}
