package sgc.mapa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MapaController.class)
@Import(RestExceptionHandler.class)
class MapaControllerTest {
    private static final String API_MAPAS = "/api/mapas";
    private static final String API_MAPAS_1 = "/api/mapas/1";
    private static final String API_MAPAS_1_ATUALIZAR = "/api/mapas/1/atualizar";
    private static final String API_MAPAS_1_EXCLUIR = "/api/mapas/1/excluir";
    private static final String CODIGO_JSON_PATH = "$.codigo";

    @MockitoBean private MapaService mapaService;
    @MockitoBean private MapaMapper mapaMapper;

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void listar_DeveRetornarListaDeMapas() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaService.listar()).thenReturn(List.of(mapa));
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get(API_MAPAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
    @WithMockUser
    void obterPorId_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaService.obterPorCodigo(1L)).thenReturn(mapa);
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    @WithMockUser
    void obterPorId_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        when(mapaService.obterPorCodigo(1L)).thenThrow(new ErroEntidadeNaoEncontrada(""));

        mockMvc.perform(get(API_MAPAS_1)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void criar_ComDadosValidos_DeveRetornarCreated() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(mapaService.criar(any(Mapa.class))).thenReturn(mapa);
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(
                        post(API_MAPAS)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_MAPAS_1));
    }

    @Test
    @WithMockUser
    void atualizar_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaService.atualizar(eq(1L), any(Mapa.class))).thenReturn(mapa);
        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(
                        post(API_MAPAS_1_ATUALIZAR)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    @WithMockUser
    void atualizar_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaService.atualizar(eq(1L), any(Mapa.class)))
                .thenThrow(new ErroEntidadeNaoEncontrada(""));
        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(new Mapa());

        mockMvc.perform(
                        post(API_MAPAS_1_ATUALIZAR)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void excluir_QuandoMapaExiste_DeveRetornarNoContent() throws Exception {
        doNothing().when(mapaService).excluir(1L);

        mockMvc.perform(post(API_MAPAS_1_EXCLUIR).with(csrf())).andExpect(status().isNoContent());

        verify(mapaService).excluir(1L);
    }

    @Test
    @WithMockUser
    void excluir_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada("")).when(mapaService).excluir(1L);

        mockMvc.perform(post(API_MAPAS_1_EXCLUIR).with(csrf())).andExpect(status().isNotFound());
    }
}
