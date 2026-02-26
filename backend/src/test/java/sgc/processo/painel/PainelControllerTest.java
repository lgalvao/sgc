package sgc.processo.painel;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.result.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PainelController.class)
@DisplayName("PainelController - Testes de Controller")
class PainelControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @MockitoBean
    private PainelFacade painelFacade;

    @Test
    @DisplayName("GET /api/painel/processos - Deve listar processos com sucesso")
    @WithMockUser
    void listarProcessos_Sucesso() throws Exception {
        Page<ProcessoResumoDto> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarProcessos(any(Perfil.class), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "ADMIN")
                        .param("unidade", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty()); // Pattern 1: Empty list validation
    }

    @Test
    @DisplayName("GET /api/painel/processos - Deve falhar sem perfil")
    @WithMockUser
    void listarProcessos_SemPerfil_DeveFalhar() throws Exception {
        mockMvc.perform(get("/api/painel/processos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/painel/alertas - Deve listar alertas com sucesso")
    @WithMockUser
    void listarAlertas_Sucesso() throws Exception {
        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarAlertas(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/alertas")
                        .param("unidade", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty()); // Pattern 1: Empty list validation
    }

    @Test
    @DisplayName("GET /api/painel/alertas - Deve listar alertas com filtros")
    @WithMockUser
    void listarAlertas_ComFiltros_Sucesso() throws Exception {
        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarAlertas(eq("123"), eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/alertas")
                        .param("usuarioTitulo", "123")
                        .param("unidade", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty()); // Pattern 1: Empty list validation
    }

}
