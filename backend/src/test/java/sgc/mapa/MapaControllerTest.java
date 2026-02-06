package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import sgc.mapa.dto.MapaDto;
import sgc.mapa.mapper.MapaMapper;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private MapaFacade mapaFacade;
    @MockitoBean
    private MapaMapper mapaMapper;

    @Autowired
    private MockMvc mockMvc;

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
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaFacade.listar()).thenReturn(List.of(mapa));
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

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
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaFacade.obterPorCodigo(1L)).thenReturn(mapa);
        when(mapaMapper.toDto(any(Mapa.class))).thenReturn(mapaDto);

        mockMvc.perform(get(API_MAPAS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound quando mapa não existir")
    void deveRetornarNotFoundQuandoMapaNaoExistir() throws Exception {
        when(mapaFacade.obterPorCodigo(1L)).thenThrow(new ErroEntidadeNaoEncontrada(""));

        mockMvc.perform(get(API_MAPAS_1)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar Created ao criar mapa")
    void deveRetornarCreatedAoCriar() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaMapper.toEntity(any(MapaDto.class))).thenReturn(mapa);
        when(mapaFacade.criar(any(Mapa.class))).thenReturn(mapa);
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
    @DisplayName("Deve retornar Ok ao atualizar mapa existente")
    void deveRetornarOkAoAtualizarMapaExistente() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        when(mapaFacade.atualizar(eq(1L), any(Mapa.class))).thenReturn(mapa);
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
    @DisplayName("Deve retornar NotFound ao atualizar mapa inexistente")
    void deveRetornarNotFoundAoAtualizarMapaInexistente() throws Exception {
        MapaDto mapaDto = MapaDto.builder().codigo(1L).build();

        when(mapaFacade.atualizar(eq(1L), any(Mapa.class)))
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
    @DisplayName("Deve retornar NoContent ao excluir mapa")
    void deveRetornarNoContentAoExcluir() throws Exception {
        doNothing().when(mapaFacade).excluir(1L);

        mockMvc.perform(post(API_MAPAS_1_EXCLUIR).with(csrf())).andExpect(status().isNoContent());

        verify(mapaFacade).excluir(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound ao excluir mapa inexistente")
    void deveRetornarNotFoundAoExcluirInexistente() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada("")).when(mapaFacade).excluir(1L);

        mockMvc.perform(post(API_MAPAS_1_EXCLUIR).with(csrf())).andExpect(status().isNotFound());
    }

    @Nested
    @DisplayName("Cobertura de Erros de Mapeamento")
    class CoberturaMapeamento {

        @Test
        @WithMockUser
        @DisplayName("Deve lançar erro quando conversão para DTO falha no obterPorId")
        void deveLancarErroQuandoConversaoParaDtoFalhaNoObterPorId() throws Exception {
            when(mapaFacade.obterPorCodigo(1L)).thenReturn(new Mapa());
            when(mapaMapper.toDto(any())).thenReturn(null);

            mockMvc.perform(get(API_MAPAS_1))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve lançar erro quando conversão para entidade falha no criar")
        void deveLancarErroQuandoConversaoParaEntidadeFalhaNoCriar() throws Exception {
            MapaDto dto = MapaDto.builder().build();
            when(mapaMapper.toEntity(any())).thenReturn(null);

            mockMvc.perform(post(API_MAPAS)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve lançar erro quando conversão para DTO falha após criar")
        void deveLancarErroQuandoConversaoParaDtoFalhaAposCriar() throws Exception {
            MapaDto dto = MapaDto.builder().build();
            Mapa entidade = new Mapa();
            when(mapaMapper.toEntity(any())).thenReturn(entidade);
            when(mapaFacade.criar(any())).thenReturn(entidade);
            when(mapaMapper.toDto(any())).thenReturn(null);

            mockMvc.perform(post(API_MAPAS)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve lançar erro quando conversão para entidade falha no atualizar")
        void deveLancarErroQuandoConversaoParaEntidadeFalhaNoAtualizar() throws Exception {
            MapaDto dto = MapaDto.builder().build();
            when(mapaMapper.toEntity(any())).thenReturn(null);

            mockMvc.perform(post(API_MAPAS_1_ATUALIZAR)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve lançar erro quando conversão para DTO falha após atualizar")
        void deveLancarErroQuandoConversaoParaDtoFalhaAposAtualizar() throws Exception {
            MapaDto dto = MapaDto.builder().build();
            Mapa entidade = new Mapa();
            when(mapaMapper.toEntity(any())).thenReturn(entidade);
            when(mapaFacade.atualizar(eq(1L), any())).thenReturn(entidade);
            when(mapaMapper.toDto(any())).thenReturn(null);

            mockMvc.perform(post(API_MAPAS_1_ATUALIZAR)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError());
        }
    }
}
