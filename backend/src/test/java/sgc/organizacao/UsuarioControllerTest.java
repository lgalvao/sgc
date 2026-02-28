package sgc.organizacao;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.*;
import org.springframework.security.test.context.support.*;
import org.springframework.security.test.web.servlet.request.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("UsuarioController - Testes de Integração")
class UsuarioControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @MockitoBean
    private UsuarioFacade usuarioService;

    @Test
    @DisplayName("GET /api/usuarios/{titulo} - Deve retornar usuário quando encontrado")
    @WithMockUser
    void buscarUsuarioPorTitulo_Sucesso() throws Exception {
        Usuario entity = new Usuario();
        entity.setTituloEleitoral("123");
        entity.setNome("Teste");
        entity.setUnidadeLotacao(Unidade.builder().codigo(1L).build());
        when(usuarioService.buscarUsuarioPorTitulo("123")).thenReturn(Optional.of(entity));

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
    @DisplayName("GET /api/usuarios/administradores - Deve listar (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void listarAdministradores_Sucesso() throws Exception {
        AdministradorDto adm = AdministradorDto.builder().nome("Admin").build();
        when(usuarioService.listarAdministradores()).thenReturn(List.of(adm));

        mockMvc.perform(get("/api/usuarios/administradores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Admin"));
    }

    @Test
    @DisplayName("POST /api/usuarios/administradores - Deve adicionar (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void adicionarAdministrador_Sucesso() throws Exception {
        AdministradorDto adm = AdministradorDto.builder().tituloEleitoral("123").nome("Admin").build();
        when(usuarioService.adicionarAdministrador("123")).thenReturn(adm);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/usuarios/administradores")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioTitulo\": \"123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloEleitoral").value("123"));
    }

    @Test
    @DisplayName("POST /api/usuarios/administradores/{usuarioTitulo}/remover - Deve remover (ADMIN)")
    void removerAdministrador_Sucesso() throws Exception {
        Usuario usuarioAtual = new Usuario();
        usuarioAtual.setTituloEleitoral("999");
        usuarioAtual.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/usuarios/administradores/123/remover")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(usuarioAtual)))
                .andExpect(status().isOk());

        ArgumentCaptor<String> captorTitulo = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorAutor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(usuarioService).removerAdministrador(captorTitulo.capture(), captorAutor.capture());

        Assertions.assertThat(captorTitulo.getValue())
                .as("Título do usuário a ser removido (PathVariable)")
                .isEqualTo("123");
    }

}
