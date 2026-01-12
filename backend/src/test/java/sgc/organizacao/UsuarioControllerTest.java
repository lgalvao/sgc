package sgc.organizacao;

import org.junit.jupiter.api.BeforeEach;
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
import tools.jackson.databind.ObjectMapper;

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
        private UsuarioService usuarioService;

        @BeforeEach
        void setUp() {
                new ObjectMapper();
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

        // ===================== ADMINISTRADORES =====================
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
