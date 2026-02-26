package sgc.parametros;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.parametros.model.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfiguracaoController.class)
@DisplayName("Testes do ConfiguracaoController")
class ConfiguracaoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ConfiguracaoService configuracaoService;
    
    @MockitoBean
    private ParametroMapper parametroMapper;

    @Test
    @DisplayName("GET /api/configuracoes - Deve listar configurações com sucesso")
    @WithMockUser(roles = "ADMIN")
    void deveListarConfiguracoes() throws Exception {
        Parametro param = Parametro.builder()
                .chave("KEY")
                .descricao("Description")
                .valor("VALUE")
                .build();
        
        when(configuracaoService.buscarTodos()).thenReturn(List.of(param));

        mockMvc.perform(get("/api/configuracoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].chave").value("KEY"))
                .andExpect(jsonPath("$[0].valor").value("VALUE"));
    }

    @Test
    @DisplayName("GET /api/configuracoes - Deve retornar lista vazia quando não há configurações")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarListaVaziaQuandoNaoHaConfiguracoes() throws Exception {
        // Pattern 1: Empty list validation
        when(configuracaoService.buscarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/configuracoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/configuracoes - Deve atualizar configurações com sucesso")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarConfiguracoes() throws Exception {
        ParametroRequest request = new ParametroRequest(1L, "KEY", "Description", "NEW_VALUE");
        Parametro response = Parametro.builder()
                .chave("KEY")
                .descricao("Description")
                .valor("NEW_VALUE")
                .build();
        
        when(configuracaoService.salvar(any())).thenReturn(List.of(response));

        mockMvc.perform(post("/api/configuracoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].chave").value("KEY"))
                .andExpect(jsonPath("$[0].valor").value("NEW_VALUE"));
    }

    @Test
    @DisplayName("POST /api/configuracoes - Deve retornar lista vazia quando atualização não retorna dados")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarListaVaziaAposAtualizacao() throws Exception {
        // Pattern 1: Empty list validation
        when(configuracaoService.salvar(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/configuracoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.emptyList())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
