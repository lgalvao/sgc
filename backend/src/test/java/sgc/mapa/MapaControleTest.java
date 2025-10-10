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
        MapaDto mapaDto = new MapaDto(1L, null, null, null, null, null);

        when(repositorioMapa.findAll()).thenReturn(List.of(mapa));
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get("/api/mapas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
    void obterPorId_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        MapaDto mapaDto = new MapaDto(1L, null, null, null, null, null);

        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapa));
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get("/api/mapas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));
    }

    @Test
    void obterPorId_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mapas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_ComDadosValidos_DeveRetornarCreated() throws Exception {
        MapaDto mapaDto = new MapaDto(1L, null, null, null, null, null);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(repositorioMapa.save(any(Mapa.class))).thenReturn(mapa);
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/mapas/1"));
    }

    @Test
    void atualizar_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        MapaDto mapaDto = new MapaDto(1L, null, null, null, null, null);
        Mapa mapaExistente = new Mapa();
        mapaExistente.setCodigo(1L);

        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapaExistente));
        when(repositorioMapa.save(any(Mapa.class))).thenReturn(mapaExistente);
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(put("/api/mapas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));
    }

    @Test
    void atualizar_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        MapaDto mapaDto = new MapaDto(1L, null, null, null, null, null);

        when(repositorioMapa.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/mapas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluir_QuandoMapaExiste_DeveRetornarNoContent() throws Exception {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(new Mapa()));

        mockMvc.perform(delete("/api/mapas/1"))
                .andExpect(status().isNoContent());

        verify(repositorioMapa, times(1)).deleteById(1L);
    }

    @Test
    void excluir_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/mapas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obterCompleto_QuandoMapaExiste_DeveRetornarOk() throws Exception {
        MapaCompletoDto mapaCompletoDto = new MapaCompletoDto(1L, 100L, "Obs", Collections.emptyList());
        when(mapaService.obterMapaCompleto(1L)).thenReturn(mapaCompletoDto);

        mockMvc.perform(get("/api/mapas/1/completo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));
    }

    @Test
    void obterCompleto_QuandoServicoLancaExcecao_DeveRetornarNotFound() throws Exception {
        when(mapaService.obterMapaCompleto(1L)).thenThrow(new RuntimeException("Erro"));

        mockMvc.perform(get("/api/mapas/1/completo"))
                .andExpect(status().isNotFound());
    }

    @Test
    void salvarCompleto_ComDadosValidos_DeveRetornarOk() throws Exception {
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", Collections.emptyList());
        MapaCompletoDto mapaCompletoDto = new MapaCompletoDto(1L, 100L, "Obs", Collections.emptyList());

        when(mapaService.salvarMapaCompleto(anyLong(), any(SalvarMapaRequest.class), anyString())).thenReturn(mapaCompletoDto);

        mockMvc.perform(put("/api/mapas/1/completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));
    }

    @Test
    void salvarCompleto_QuandoServicoLancaExcecao_DeveRetornarBadRequest() throws Exception {
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", Collections.emptyList());
        when(mapaService.salvarMapaCompleto(anyLong(), any(SalvarMapaRequest.class), anyString())).thenThrow(new RuntimeException("Erro"));

        mockMvc.perform(put("/api/mapas/1/completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
