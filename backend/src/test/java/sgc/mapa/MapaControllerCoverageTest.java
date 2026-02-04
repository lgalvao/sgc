package sgc.mapa;

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
import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.mapper.MapaMapper;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MapaController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("Testes de Cobertura do MapaController")
class MapaControllerCoverageTest {

    @MockitoBean
    private MapaFacade mapaFacade;

    @MockitoBean
    private MapaMapper mapaMapper;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    @DisplayName("Deve lançar erro quando conversão para DTO falha no obterPorId")
    void deveLancarErroQuandoConversaoParaDtoFalhaNoObterPorId() throws Exception {
        when(mapaFacade.obterPorCodigo(1L)).thenReturn(new Mapa());
        when(mapaMapper.toDto(any())).thenReturn(null);

        mockMvc.perform(get("/api/mapas/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve lançar erro quando conversão para entidade falha no criar")
    void deveLancarErroQuandoConversaoParaEntidadeFalhaNoCriar() throws Exception {
        MapaDto dto = MapaDto.builder().build();
        when(mapaMapper.toEntity(any())).thenReturn(null);

        mockMvc.perform(post("/api/mapas")
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

        mockMvc.perform(post("/api/mapas")
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

        mockMvc.perform(post("/api/mapas/1/atualizar")
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

        mockMvc.perform(post("/api/mapas/1/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }
}
