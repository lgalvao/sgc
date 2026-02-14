package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.Usuario;


import java.util.List;
import java.util.Optional;

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
    private UsuarioFacade usuarioService;

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
    @DisplayName("GET /api/usuarios/administradores - Deve retornar lista vazia quando não há administradores")
    @WithMockUser(roles = "ADMIN")
    void listarAdministradores_DeveRetornarListaVaziaQuandoNaoHaAdministradores() throws Exception {
        when(usuarioService.listarAdministradores()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios/administradores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    @Test
    @DisplayName("POST /api/usuarios/administradores - Deve adicionar (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void adicionarAdministrador_Sucesso() throws Exception {
        AdministradorDto adm = AdministradorDto.builder().tituloEleitoral("123").nome("Admin").build();
        when(usuarioService.adicionarAdministrador("123")).thenReturn(adm);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/usuarios/administradores")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"usuarioTitulo\": \"123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloEleitoral").value("123"));
    }

    @Test
    @DisplayName("POST /api/usuarios/administradores/{usuarioTitulo}/remover - Deve remover (ADMIN)")
    void removerAdministrador_Sucesso() throws Exception {
        Usuario usuarioAtual = Usuario.builder().tituloEleitoral("999").build();
        usuarioAtual.setAuthorities(java.util.Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/usuarios/administradores/123/remover")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(usuarioAtual, null, usuarioAtual.getAuthorities())
                        )))
                .andExpect(status().isOk());
        
        org.mockito.ArgumentCaptor<String> captorTitulo = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> captorAutor = org.mockito.ArgumentCaptor.forClass(String.class);
        
        org.mockito.Mockito.verify(usuarioService).removerAdministrador(captorTitulo.capture(), captorAutor.capture());
        
        org.assertj.core.api.Assertions.assertThat(captorTitulo.getValue()).isEqualTo("123");
        org.assertj.core.api.Assertions.assertThat(captorAutor.getValue()).isEqualTo("999");
    }

}
