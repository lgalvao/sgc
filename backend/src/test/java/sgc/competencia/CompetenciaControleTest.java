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
    private CompetenciaRepo competenciaRepo;
    @MockitoBean
    private CompetenciaMapper competenciaMapper;
    @MockitoBean
    private AtividadeRepo atividadeRepo;
    @MockitoBean
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @Test
    void listarCompetencias_deveRetornarListaDeCompetencias() throws Exception {
        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao(TEST_DESC);

        CompetenciaDto competenciaDTO = new CompetenciaDto(1L, null, TEST_DESC);

        when(competenciaRepo.findAll()).thenReturn(Collections.singletonList(competencia));
        when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(competenciaDTO);

        // When & Then
        mockMvc.perform(get("/api/competencias").with(user(TESTUSER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].codigo").value(1L))
                .andExpect(jsonPath("$[0].descricao").value(TEST_DESC));
    }

    @Test
    void obterCompetencia_quandoEncontrada_deveRetornarCompetencia() throws Exception {
        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao(TEST_DESC);

        CompetenciaDto competenciaDTO = new CompetenciaDto(1L, null, TEST_DESC);

        when(competenciaRepo.findById(1L)).thenReturn(Optional.of(competencia));
        when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(competenciaDTO);

        mockMvc.perform(get(API_COMPETENCIAS_1).with(user(TESTUSER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value(TEST_DESC));
    }

    @Test
    void obterCompetencia_quandoNaoEncontrada_deveRetornarNotFound() throws Exception {
        when(competenciaRepo.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_COMPETENCIAS_1).with(user(TESTUSER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarCompetencia_deveCriarEretornarCompetencia() throws Exception {
        CompetenciaDto dto = new CompetenciaDto(null, null, "Nova Competencia");

        Competencia entity = new Competencia();
        entity.setDescricao(dto.descricao());

        Competencia savedEntity = new Competencia();
        savedEntity.setCodigo(1L);
        savedEntity.setDescricao(entity.getDescricao());

        CompetenciaDto savedDto = new CompetenciaDto(savedEntity.getCodigo(), null, savedEntity.getDescricao());

        when(competenciaMapper.toEntity(any(CompetenciaDto.class))).thenReturn(entity);
        when(competenciaRepo.save(any(Competencia.class))).thenReturn(savedEntity);
        when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(savedDto);

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
        CompetenciaDto dto = new CompetenciaDto(null, null, "Competencia Atualizada");

        Competencia existingEntity = new Competencia();
        existingEntity.setCodigo(1L);
        existingEntity.setDescricao("Competencia Antiga");

        Competencia updatedEntity = new Competencia();
        updatedEntity.setCodigo(1L);
        updatedEntity.setDescricao(dto.descricao());

        CompetenciaDto updatedDto = new CompetenciaDto(1L, null, dto.descricao());

        when(competenciaRepo.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(competenciaRepo.save(any(Competencia.class))).thenReturn(updatedEntity);
        when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(updatedDto);

        mockMvc.perform(put(API_COMPETENCIAS_1)
                        .with(user(TESTUSER)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Competencia Atualizada"));
    }

    @Test
    void excluirCompetencia_quandoEncontrada_deveRetornarNoContent() throws Exception {
        Competencia existingEntity = new Competencia();
        existingEntity.setCodigo(1L);
        when(competenciaRepo.findById(1L)).thenReturn(Optional.of(existingEntity));
        doNothing().when(competenciaRepo).deleteById(1L);

        // When & Then
        mockMvc.perform(delete(API_COMPETENCIAS_1).with(user(TESTUSER)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    // Testes para os novos endpoints de vínculo
    @Test
    void vincularAtividade_deveCriarVinculo() throws Exception {
        long idCompetencia = 1L;
        long idAtividade = 2L;

        when(competenciaRepo.findById(idCompetencia)).thenReturn(Optional.of(new Competencia()));
        when(atividadeRepo.findById(idAtividade)).thenReturn(Optional.of(new Atividade()));
        when(competenciaAtividadeRepo.existsById(any())).thenReturn(false);
        when(competenciaAtividadeRepo.save(any())).thenReturn(new CompetenciaAtividade());

        String requestBody = "{\"idAtividade\": " + idAtividade + "}";

        mockMvc.perform(post("/api/competencias/{idCompetencia}/atividades", idCompetencia)
                        .with(user(TESTUSER)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void desvincularAtividade_deveRemoverVinculo() throws Exception {
        long idCompetencia = 1L;
        long idAtividade = 2L;
        CompetenciaAtividade.Id vinculoId = new CompetenciaAtividade.Id(idAtividade, idCompetencia);

        when(competenciaAtividadeRepo.existsById(vinculoId)).thenReturn(true);
        doNothing().when(competenciaAtividadeRepo).deleteById(vinculoId);

        mockMvc.perform(delete("/api/competencias/{idCompetencia}/atividades/{idAtividade}", idCompetencia, idAtividade)
                        .with(user(TESTUSER)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarAtividadesVinculadas_deveRetornarLista() throws Exception {
        long idCompetencia = 1L;
        when(competenciaRepo.existsById(idCompetencia)).thenReturn(true);
        when(competenciaAtividadeRepo.findAll()).thenReturn(List.of()); // Simplesmente retorna lista vazia

        mockMvc.perform(get("/api/competencias/{idCompetencia}/atividades", idCompetencia)
                        .with(user(TESTUSER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}