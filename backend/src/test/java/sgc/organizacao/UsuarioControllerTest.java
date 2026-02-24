package sgc.organizacao;
import sgc.seguranca.SgcPermissionEvaluator;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
