package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.dto.AtualizarEstadoMapaCommand;
import sgc.mapa.dto.AtualizarMapaRequest;
import sgc.mapa.dto.CriarMapaCommand;
import sgc.mapa.dto.CriarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.Subprocesso;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MapaController.class)
@Import({RestExceptionHandler.class, MapaDtoMapper.class})
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar lista de mapas")
    void deveRetornarListaDeMapas() throws Exception {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa.setCodigo(1L);

        org.springframework.data.domain.Page<Mapa> page = new org.springframework.data.domain.PageImpl<>(List.of(mapa));
        when(mapaManutencaoService.mapas(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get(API_MAPAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar mapa quando existir")
    void deveRetornarMapaQuandoExistir() throws Exception {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);

        when(mapaManutencaoService.mapaCodigo(1L)).thenReturn(mapa);

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound quando mapa não existir")
    void deveRetornarNotFoundQuandoMapaNaoExistir() throws Exception {
        when(mapaManutencaoService.mapaCodigo(999L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 999L));

        mockMvc.perform(get("/api/mapas/999"))
                .andExpect(status().isNotFound());

        verify(mapaManutencaoService).mapaCodigo(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar Created ao criar mapa")
    void deveRetornarCreatedAoCriar() throws Exception {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        CriarMapaRequest request = CriarMapaRequest.builder().subprocessoCodigo(10L).build();

        when(mapaManutencaoService.criarMapa(any(CriarMapaCommand.class))).thenReturn(mapa);

        mockMvc.perform(post(API_MAPAS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith(API_MAPAS_1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar Ok ao atualizar mapa existente")
    void deveRetornarOkAoAtualizarMapaExistente() throws Exception {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        AtualizarMapaRequest request = AtualizarMapaRequest.builder().observacoesDisponibilizacao("Obs").build();

        when(mapaManutencaoService.atualizarEstadoMapa(eq(1L), any(AtualizarEstadoMapaCommand.class))).thenReturn(mapa);

        mockMvc.perform(post(API_MAPAS_1_ATUALIZAR)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
