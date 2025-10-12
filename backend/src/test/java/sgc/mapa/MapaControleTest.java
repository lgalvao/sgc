package sgc.mapa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MapaControleTest {
    private static final String API_MAPAS = "/api/mapas";
    private static final String API_MAPAS_1 = "/api/mapas/1";
    private static final String API_MAPAS_1_COMPLETO = "/api/mapas/1/completo";
    private static final String CODIGO_JSON_PATH = "$.codigo";
    private static final String OBS = "Obs";

    @Mock
    private MapaRepo repositorioMapa;

    @Mock
    private MapaService mapaService;

    @Mock
    private MapaMapper mapaMapper;

    @InjectMocks
    private MapaControle mapaControle;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mapaControle).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void listar_DeveRetornarListaDeMapas() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(repositorioMapa.findAll()).thenReturn(List.of(mapa));
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get(API_MAPAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
    void obterPorId_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapa));
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    void obterPorId_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_ComDadosValidos_DeveRetornarCreated() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(repositorioMapa.save(any(Mapa.class))).thenReturn(mapa);
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(post(API_MAPAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_MAPAS_1));
    }

    @Test
    void atualizar_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapaExistente = new Mapa();
        mapaExistente.setCodigo(1L);

        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapaExistente));
        when(repositorioMapa.save(any(Mapa.class))).thenReturn(mapaExistente);
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(put(API_MAPAS_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    void atualizar_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(repositorioMapa.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put(API_MAPAS_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluir_QuandoMapaExiste_DeveRetornarNoContent() throws Exception {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(new Mapa()));

        mockMvc.perform(delete(API_MAPAS_1))
                .andExpect(status().isNoContent());

        verify(repositorioMapa, times(1)).deleteById(1L);
    }

    @Test
    void excluir_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete(API_MAPAS_1))
                .andExpect(status().isNotFound());
    }

    @Test
    void obterCompleto_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        MapaCompletoDto mapaCompletoDto = new MapaCompletoDto(1L, 100L, OBS, Collections.emptyList());
        when(mapaService.obterMapaCompleto(1L)).thenReturn(mapaCompletoDto);

        mockMvc.perform(get(API_MAPAS_1_COMPLETO))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    void obterCompleto_QuandoServicoLancaExcecao_DeveRetornarNotFound() throws Exception {
        when(mapaService.obterMapaCompleto(1L)).thenThrow(new RuntimeException("Erro"));

        mockMvc.perform(get(API_MAPAS_1_COMPLETO))
                .andExpect(status().isNotFound());
    }

    @Test
    void salvarCompleto_ComDadosValidos_DeveRetornarOk() throws Exception {
        SalvarMapaRequest request = new SalvarMapaRequest(OBS, Collections.emptyList());
        MapaCompletoDto mapaCompletoDto = new MapaCompletoDto(1L, 100L, OBS, Collections.emptyList());

        when(mapaService.salvarMapaCompleto(anyLong(), any(SalvarMapaRequest.class), anyString())).thenReturn(mapaCompletoDto);

        mockMvc.perform(put(API_MAPAS_1_COMPLETO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    void salvarCompleto_QuandoServicoLancaExcecao_DeveRetornarBadRequest() throws Exception {
        SalvarMapaRequest request = new SalvarMapaRequest(OBS, Collections.emptyList());
        when(mapaService.salvarMapaCompleto(anyLong(), any(SalvarMapaRequest.class), anyString())).thenThrow(new RuntimeException("Erro"));

        mockMvc.perform(put(API_MAPAS_1_COMPLETO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
