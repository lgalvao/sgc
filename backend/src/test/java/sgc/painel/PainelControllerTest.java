package sgc.painel;
import sgc.seguranca.SgcPermissionEvaluator;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.alerta.model.Alerta;
import sgc.organizacao.model.Perfil;
import sgc.processo.dto.ProcessoResumoDto;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
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
