package sgc.competencia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetenciaControle.class)
class CompetenciaControleTest {
    private static final String TEST_DESC = "Test Desc";
    private static final String TESTUSER = "testuser";
    private static final String API_COMPETENCIAS_1 = "/api/competencias/1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompetenciaService competenciaService;
    @MockitoBean
    private CompetenciaMapper competenciaMapper;

    @Test
    void listarCompetencias_deveRetornarListaDeCompetencias() throws Exception {
        CompetenciaDto competenciaDTO = new CompetenciaDto(1L, null, TEST_DESC);

        when(competenciaService.listarCompetencias()).thenReturn(Collections.singletonList(competenciaDTO));

        // When & Then
        mockMvc.perform(get("/api/competencias").with(user(TESTUSER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].codigo").value(1L))
                .andExpect(jsonPath("$[0].descricao").value(TEST_DESC));
    }

    @Test
    void obterCompetencia_quandoEncontrada_deveRetornarCompetencia() throws Exception {
        CompetenciaDto competenciaDTO = new CompetenciaDto(1L, null, TEST_DESC);

        when(competenciaService.obterCompetencia(1L)).thenReturn(competenciaDTO);

        mockMvc.perform(get(API_COMPETENCIAS_1).with(user(TESTUSER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value(TEST_DESC));
    }

    @Test
    void obterCompetencia_quandoNaoEncontrada_deveRetornarNotFound() throws Exception {
        when(competenciaService.obterCompetencia(1L)).thenThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(""));

        mockMvc.perform(get(API_COMPETENCIAS_1).with(user(TESTUSER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarCompetencia_deveCriarEretornarCompetencia() throws Exception {
        CompetenciaDto dto = new CompetenciaDto(null, null, "Nova Competencia");
        CompetenciaDto savedDto = new CompetenciaDto(1L, null, "Nova Competencia");

        when(competenciaService.criarCompetencia(any(CompetenciaDto.class))).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/api/competencias")
                        .with(user(TESTUSER)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_COMPETENCIAS_1))
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Nova Competencia"));
    }

    @Test
    void atualizarCompetencia_quandoEncontrada_deveAtualizarEretornarCompetencia() throws Exception {
        CompetenciaDto dto = new CompetenciaDto(1L, null, "Competencia Atualizada");
        CompetenciaDto updatedDto = new CompetenciaDto(1L, null, "Competencia Atualizada");

        when(competenciaService.atualizarCompetencia(eq(1L), any(CompetenciaDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put(API_COMPETENCIAS_1)
                        .with(user(TESTUSER)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Competencia Atualizada"));
    }

    @Test
    void excluirCompetencia_quandoEncontrada_deveRetornarNoContent() throws Exception {
        doNothing().when(competenciaService).excluirCompetencia(1L);

        // When & Then
        mockMvc.perform(delete(API_COMPETENCIAS_1).with(user(TESTUSER)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    // Testes para os novos endpoints de vínculo
    @Test
    void vincularAtividade_deveCriarVinculo() throws Exception {
        long idCompetencia = 1L;
        long idAtividade = 2L;

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(new CompetenciaAtividade.Id(idAtividade, idCompetencia));
        when(competenciaService.vincularAtividade(idCompetencia, idAtividade)).thenReturn(vinculo);

        String requestBody = "{\"idAtividade\": " + idAtividade + "}";

        mockMvc.perform(post("/api/competencias/{idCompetencia}/atividades", idCompetencia)
                        .with(user(TESTUSER)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/competencias/1/atividades/2"));
    }

    @Test
    void desvincularAtividade_deveRemoverVinculo() throws Exception {
        long idCompetencia = 1L;
        long idAtividade = 2L;

        doNothing().when(competenciaService).desvincularAtividade(idCompetencia, idAtividade);

        mockMvc.perform(delete("/api/competencias/{idCompetencia}/atividades/{idAtividade}", idCompetencia, idAtividade)
                        .with(user(TESTUSER)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarAtividadesVinculadas_deveRetornarLista() throws Exception {
        long idCompetencia = 1L;
        when(competenciaService.listarAtividadesVinculadas(idCompetencia)).thenReturn(List.of());

        mockMvc.perform(get("/api/competencias/{idCompetencia}/atividades", idCompetencia)
                        .with(user(TESTUSER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}