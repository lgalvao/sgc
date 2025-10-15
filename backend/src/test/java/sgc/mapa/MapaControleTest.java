package sgc.mapa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.sgrh.Usuario;

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
    private MapaService mapaService;

    @Mock
    private MapaCrudService mapaCrudService;

    @Mock
    private MapaMapper mapaMapper;

    @InjectMocks
    private MapaControle mapaControle;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(123456789012L);

        HandlerMethodArgumentResolver authenticationPrincipalResolver = new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver();

        mockMvc = MockMvcBuilders.standaloneSetup(mapaControle)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void listar_DeveRetornarListaDeMapas() throws Exception {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaCrudService.listar()).thenReturn(List.of(mapa));
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

        when(mapaCrudService.obterPorId(1L)).thenReturn(mapa);
        when(mapaMapper.toDTO(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    void obterPorId_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        when(mapaCrudService.obterPorId(1L)).thenThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(""));

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_ComDadosValidos_DeveRetornarCreated() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(mapaCrudService.criar(any(Mapa.class))).thenReturn(mapa);
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
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaCrudService.atualizar(eq(1L), any(Mapa.class))).thenReturn(mapa);
        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
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

        when(mapaCrudService.atualizar(eq(1L), any(Mapa.class))).thenThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(""));
        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(new Mapa());

        mockMvc.perform(put(API_MAPAS_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluir_QuandoMapaExiste_DeveRetornarNoContent() throws Exception {
        doNothing().when(mapaCrudService).excluir(1L);

        mockMvc.perform(delete(API_MAPAS_1))
                .andExpect(status().isNoContent());

        verify(mapaCrudService, times(1)).excluir(1L);
    }

    @Test
    void excluir_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        doThrow(new sgc.comum.erros.ErroDominioNaoEncontrado("")).when(mapaCrudService).excluir(1L);

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

        when(mapaService.salvarMapaCompleto(anyLong(), any(SalvarMapaRequest.class), anyLong())).thenReturn(mapaCompletoDto);

        mockMvc.perform(put(API_MAPAS_1_COMPLETO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    void salvarCompleto_QuandoServicoLancaExcecao_DeveRetornarBadRequest() throws Exception {
        SalvarMapaRequest request = new SalvarMapaRequest(OBS, Collections.emptyList());
        when(mapaService.salvarMapaCompleto(anyLong(), any(SalvarMapaRequest.class), anyLong())).thenThrow(new RuntimeException("Erro"));

        mockMvc.perform(put(API_MAPAS_1_COMPLETO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
