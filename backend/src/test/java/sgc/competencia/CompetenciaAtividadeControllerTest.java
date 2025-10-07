package sgc.competencia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeRepository;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetenciaAtividadeController.class)
class CompetenciaAtividadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompetenciaAtividadeRepository competenciaAtividadeRepository;

    @MockitoBean
    private AtividadeRepository atividadeRepository;

    @MockitoBean
    private CompetenciaRepository competenciaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarVinculos_deveRetornarListaDeVinculos() throws Exception {
        // Given
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);

        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);

        CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(1L, 1L);
        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(atividade);
        vinculo.setCompetencia(competencia);

        when(competenciaAtividadeRepository.findAll()).thenReturn(Collections.singletonList(vinculo));

        // When & Then
        mockMvc.perform(get("/api/competencia-atividades").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id.atividadeCodigo").value(1L))
                .andExpect(jsonPath("$[0].id.competenciaCodigo").value(1L));
    }

    @Test
    void listarPorAtividade_deveRetornarListaDeVinculos() throws Exception {
        // Given
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);

        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);

        CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(1L, 1L);
        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(atividade);
        vinculo.setCompetencia(competencia);

        when(competenciaAtividadeRepository.findAll()).thenReturn(Collections.singletonList(vinculo));

        // When & Then
        mockMvc.perform(get("/api/competencia-atividades/por-atividade/1").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id.atividadeCodigo").value(1L));
    }

    @Test
    void vincular_comSucesso_deveCriarVinculo() throws Exception {
        // Given
        CompetenciaAtividadeController.VinculoRequest request = new CompetenciaAtividadeController.VinculoRequest();
        request.setAtividadeCodigo(1L);
        request.setCompetenciaCodigo(1L);

        when(atividadeRepository.findById(1L)).thenReturn(Optional.of(new Atividade()));
        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(new Competencia()));
        when(competenciaAtividadeRepository.existsById(any())).thenReturn(false);
        when(competenciaAtividadeRepository.save(any())).thenReturn(new CompetenciaAtividade());

        // When & Then
        mockMvc.perform(post("/api/competencia-atividades")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void vincular_quandoAtividadeNaoEncontrada_deveRetornarBadRequest() throws Exception {
        // Given
        CompetenciaAtividadeController.VinculoRequest request = new CompetenciaAtividadeController.VinculoRequest();
        request.setAtividadeCodigo(99L); // Non-existent
        request.setCompetenciaCodigo(1L);

        when(atividadeRepository.findById(99L)).thenReturn(Optional.empty());
        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(new Competencia()));

        // When & Then
        mockMvc.perform(post("/api/competencia-atividades")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vincular_quandoCompetenciaNaoEncontrada_deveRetornarBadRequest() throws Exception {
        // Given
        CompetenciaAtividadeController.VinculoRequest request = new CompetenciaAtividadeController.VinculoRequest();
        request.setAtividadeCodigo(1L);
        request.setCompetenciaCodigo(99L); // Non-existent

        when(atividadeRepository.findById(1L)).thenReturn(Optional.of(new Atividade()));
        when(competenciaRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/competencia-atividades")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vincular_quandoVinculoJaExiste_deveRetornarConflict() throws Exception {
        // Given
        CompetenciaAtividadeController.VinculoRequest request = new CompetenciaAtividadeController.VinculoRequest();
        request.setAtividadeCodigo(1L);
        request.setCompetenciaCodigo(1L);

        when(atividadeRepository.findById(1L)).thenReturn(Optional.of(new Atividade()));
        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(new Competencia()));
        when(competenciaAtividadeRepository.existsById(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/competencia-atividades")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void desvincular_comSucesso_deveRemoverVinculo() throws Exception {
        // Given
        when(competenciaAtividadeRepository.existsById(any())).thenReturn(true);
        doNothing().when(competenciaAtividadeRepository).deleteById(any());

        // When & Then
        mockMvc.perform(delete("/api/competencia-atividades?atividadeCodigo=1&competenciaCodigo=1")
                .with(user("testuser")).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void desvincular_quandoNaoEncontrado_deveRetornarNotFound() throws Exception {
        // Given
        when(competenciaAtividadeRepository.existsById(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/competencia-atividades?atividadeCodigo=1&competenciaCodigo=1")
                .with(user("testuser")).with(csrf()))
                .andExpect(status().isNotFound());
    }
}