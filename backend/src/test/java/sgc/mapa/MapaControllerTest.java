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
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaService;
import sgc.sgrh.model.Usuario;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MapaControllerTest {
    private static final String API_MAPAS = "/api/mapas";
    private static final String API_MAPAS_1 = "/api/mapas/1";
    private static final String API_MAPAS_1_ATUALIZAR = "/api/mapas/1/atualizar";
    private static final String API_MAPAS_1_EXCLUIR = "/api/mapas/1/excluir";
    private static final String CODIGO_JSON_PATH = "$.codigo";

    @Mock
    private MapaService mapaService;

    @Mock
    private MapaMapper mapaMapper;

    @InjectMocks
    private MapaController mapaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");

        HandlerMethodArgumentResolver authenticationPrincipalResolver = new AuthenticationPrincipalArgumentResolver();

        mockMvc = MockMvcBuilders.standaloneSetup(mapaController)
                .setControllerAdvice(new sgc.comum.erros.RestExceptionHandler())
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

        when(mapaService.listar()).thenReturn(List.of(mapa));
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get(API_MAPAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
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
    void obterPorId_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        when(mapaService.obterPorCodigo(1L)).thenThrow(new ErroEntidadeNaoEncontrada(""));

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_ComDadosValidos_DeveRetornarCreated() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(mapaService.criar(any(Mapa.class))).thenReturn(mapa);
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

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

        when(mapaService.atualizar(eq(1L), any(Mapa.class))).thenReturn(mapa);
        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(post(API_MAPAS_1_ATUALIZAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    void atualizar_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaService.atualizar(eq(1L), any(Mapa.class))).thenThrow(new ErroEntidadeNaoEncontrada(""));
        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(new Mapa());

        mockMvc.perform(post(API_MAPAS_1_ATUALIZAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mapaDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluir_QuandoMapaExiste_DeveRetornarNoContent() throws Exception {
        doNothing().when(mapaService).excluir(1L);

        mockMvc.perform(post(API_MAPAS_1_EXCLUIR))
                .andExpect(status().isNoContent());

        verify(mapaService, times(1)).excluir(1L);
    }

    @Test
    void excluir_QuandoMapaNaoExiste_DeveRetornarNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada("")).when(mapaService).excluir(1L);

        mockMvc.perform(post(API_MAPAS_1_EXCLUIR))
                .andExpect(status().isNotFound());
    }
}
