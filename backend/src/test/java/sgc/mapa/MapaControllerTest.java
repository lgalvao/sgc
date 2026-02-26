package sgc.mapa;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.seguranca.*;
import tools.jackson.databind.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MapaController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("Testes do Controller de Mapas")
class MapaControllerTest {
    private static final String API_MAPAS = "/api/mapas";
    private static final String API_MAPAS_1 = "/api/mapas/1";
    private static final String API_MAPAS_1_ATUALIZAR = "/api/mapas/1/atualizar";
    private static final String API_MAPAS_1_EXCLUIR = "/api/mapas/1/excluir";
    private static final String CODIGO_JSON_PATH = "$.codigo";

    @MockitoBean
    private MapaManutencaoService mapaManutencaoService;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar lista de mapas")
    void deveRetornarListaDeMapas() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaManutencaoService.listarTodosMapas()).thenReturn(List.of(mapa));

        mockMvc.perform(get(API_MAPAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar mapa quando existir")
    void deveRetornarMapaQuandoExistir() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaManutencaoService.buscarMapaPorCodigo(1L)).thenReturn(mapa);

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound quando mapa não existir")
    void deveRetornarNotFoundQuandoMapaNaoExistir() throws Exception {
        when(mapaManutencaoService.buscarMapaPorCodigo(999L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 999L));

        mockMvc.perform(get("/api/mapas/999"))
                .andExpect(status().isNotFound());

        verify(mapaManutencaoService).buscarMapaPorCodigo(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar Created ao criar mapa")
    void deveRetornarCreatedAoCriar() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaManutencaoService.salvarMapa(any(Mapa.class))).thenReturn(mapa);

        mockMvc.perform(post(API_MAPAS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapa)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_MAPAS_1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar Ok ao atualizar mapa existente")
    void deveRetornarOkAoAtualizarMapaExistente() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaManutencaoService.buscarMapaPorCodigo(1L)).thenReturn(mapa);
        when(mapaManutencaoService.salvarMapa(any(Mapa.class))).thenReturn(mapa);

        mockMvc.perform(post(API_MAPAS_1_ATUALIZAR)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar NoContent ao excluir mapa")
    void deveRetornarNoContentAoExcluir() throws Exception {
        doNothing().when(mapaManutencaoService).excluirMapa(1L);

        mockMvc.perform(post(API_MAPAS_1_EXCLUIR).with(csrf())).andExpect(status().isNoContent());

        verify(mapaManutencaoService).excluirMapa(1L);
    }
}
