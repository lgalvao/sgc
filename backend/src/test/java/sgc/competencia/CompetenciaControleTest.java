package sgc.competencia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaRepo;

import java.util.Collections;
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
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompetenciaRepo competenciaRepo;

    @MockitoBean
    private CompetenciaMapper competenciaMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarCompetencias_deveRetornarListaDeCompetencias() throws Exception {
        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao("Test Desc");

        CompetenciaDto competenciaDTO = new CompetenciaDto(1L, null, "Test Desc");

        when(competenciaRepo.findAll()).thenReturn(Collections.singletonList(competencia));
        when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(competenciaDTO);

        // When & Then
        mockMvc.perform(get("/api/competencias").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].codigo").value(1L))
                .andExpect(jsonPath("$[0].descricao").value("Test Desc"));
    }

    @Test
    void obterCompetencia_quandoEncontrada_deveRetornarCompetencia() throws Exception {
        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao("Test Desc");

        CompetenciaDto competenciaDTO = new CompetenciaDto(1L, null, "Test Desc");

        when(competenciaRepo.findById(1L)).thenReturn(Optional.of(competencia));
        when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(competenciaDTO);

        // When & Then
        mockMvc.perform(get("/api/competencias/1").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Test Desc"));
    }

    @Test
    void obterCompetencia_quandoNaoEncontrada_deveRetornarNotFound() throws Exception {
        when(competenciaRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/competencias/1").with(user("testuser")))
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
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/competencias/1"))
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Nova Competencia"));
    }

    @Test
    void criarCompetencia_comDescricaoInvalida_deveRetornarBadRequest() throws Exception {
        CompetenciaDto dto = new CompetenciaDto(null, null, " "); // Descrição inválida

        // When & Then
        mockMvc.perform(post("/api/competencias")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
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

        // When & Then
        mockMvc.perform(put("/api/competencias/1")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Competencia Atualizada"));
    }

    @Test
    void atualizarCompetencia_comDescricaoInvalida_deveRetornarBadRequest() throws Exception {
        CompetenciaDto dto = new CompetenciaDto(null, null, ""); // Descrição inválida

        when(competenciaRepo.findById(1L)).thenReturn(Optional.of(new Competencia()));

        // When & Then
        mockMvc.perform(put("/api/competencias/1")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarCompetencia_quandoNaoEncontrada_deveRetornarNotFound() throws Exception {
        CompetenciaDto dto = new CompetenciaDto(null, null, "Nao importa");
        when(competenciaRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/competencias/1")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirCompetencia_quandoEncontrada_deveRetornarNoContent() throws Exception {
        Competencia existingEntity = new Competencia();
        existingEntity.setCodigo(1L);
        when(competenciaRepo.findById(1L)).thenReturn(Optional.of(existingEntity));
        doNothing().when(competenciaRepo).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/competencias/1").with(user("testuser")).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void excluirCompetencia_quandoNaoEncontrada_deveRetornarNotFound() throws Exception {
        when(competenciaRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/competencias/1").with(user("testuser")).with(csrf()))
                .andExpect(status().isNotFound());
    }
}