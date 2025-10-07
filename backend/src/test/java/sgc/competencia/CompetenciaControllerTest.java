package sgc.competencia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetenciaController.class)
class CompetenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompetenciaRepository competenciaRepository;

    @MockitoBean
    private CompetenciaMapper competenciaMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarCompetencias_deveRetornarListaDeCompetencias() throws Exception {
        // Given
        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao("Test Desc");

        CompetenciaDTO competenciaDTO = new CompetenciaDTO();
        competenciaDTO.setCodigo(1L);
        competenciaDTO.setDescricao("Test Desc");

        when(competenciaRepository.findAll()).thenReturn(Collections.singletonList(competencia));
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
        // Given
        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao("Test Desc");

        CompetenciaDTO competenciaDTO = new CompetenciaDTO();
        competenciaDTO.setCodigo(1L);
        competenciaDTO.setDescricao("Test Desc");

        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(competencia));
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
        // Given
        when(competenciaRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/competencias/1").with(user("testuser")))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarCompetencia_deveCriarEretornarCompetencia() throws Exception {
        // Given
        CompetenciaDTO dto = new CompetenciaDTO();
        dto.setDescricao("Nova Competencia");

        Competencia entity = new Competencia();
        entity.setDescricao(dto.getDescricao());

        Competencia savedEntity = new Competencia();
        savedEntity.setCodigo(1L);
        savedEntity.setDescricao(entity.getDescricao());

        CompetenciaDTO savedDto = new CompetenciaDTO();
        savedDto.setCodigo(savedEntity.getCodigo());
        savedDto.setDescricao(savedEntity.getDescricao());

        when(competenciaMapper.toEntity(any(CompetenciaDTO.class))).thenReturn(entity);
        when(competenciaRepository.save(any(Competencia.class))).thenReturn(savedEntity);
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
        // Given
        CompetenciaDTO dto = new CompetenciaDTO();
        dto.setDescricao(" "); // Descrição inválida

        // When & Then
        mockMvc.perform(post("/api/competencias")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarCompetencia_quandoEncontrada_deveAtualizarEretornarCompetencia() throws Exception {
        // Given
        CompetenciaDTO dto = new CompetenciaDTO();
        dto.setDescricao("Competencia Atualizada");

        Competencia existingEntity = new Competencia();
        existingEntity.setCodigo(1L);
        existingEntity.setDescricao("Competencia Antiga");

        Competencia updatedEntity = new Competencia();
        updatedEntity.setCodigo(1L);
        updatedEntity.setDescricao(dto.getDescricao());

        CompetenciaDTO updatedDto = new CompetenciaDTO();
        updatedDto.setCodigo(1L);
        updatedDto.setDescricao(dto.getDescricao());

        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(competenciaRepository.save(any(Competencia.class))).thenReturn(updatedEntity);
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
        // Given
        CompetenciaDTO dto = new CompetenciaDTO();
        dto.setDescricao(""); // Descrição inválida

        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(new Competencia()));

        // When & Then
        mockMvc.perform(put("/api/competencias/1")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarCompetencia_quandoNaoEncontrada_deveRetornarNotFound() throws Exception {
        // Given
        CompetenciaDTO dto = new CompetenciaDTO();
        dto.setDescricao("Nao importa");
        when(competenciaRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/competencias/1")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirCompetencia_quandoEncontrada_deveRetornarNoContent() throws Exception {
        // Given
        Competencia existingEntity = new Competencia();
        existingEntity.setCodigo(1L);
        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        doNothing().when(competenciaRepository).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/competencias/1").with(user("testuser")).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void excluirCompetencia_quandoNaoEncontrada_deveRetornarNotFound() throws Exception {
        // Given
        when(competenciaRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/competencias/1").with(user("testuser")).with(csrf()))
                .andExpect(status().isNotFound());
    }
}