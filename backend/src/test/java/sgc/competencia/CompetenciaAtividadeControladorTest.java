package sgc.competencia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.atividade.Atividade;
import sgc.atividade.RepositorioAtividade;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetenciaAtividadeControlador.class)
class CompetenciaAtividadeControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompetenciaAtividadeRepository repositorioCompetenciaAtividade;

    @MockitoBean
    private RepositorioAtividade repositorioAtividade;

    @MockitoBean
    private CompetenciaRepository repositorioCompetencia;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listar_deveRetornarListaDeVinculos() throws Exception {
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);

        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);

        CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(1L, 1L);
        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(atividade);
        vinculo.setCompetencia(competencia);

        when(repositorioCompetenciaAtividade.findAll()).thenReturn(Collections.singletonList(vinculo));

        mockMvc.perform(get("/api/competencia-atividades").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id.atividadeCodigo").value(1L))
                .andExpect(jsonPath("$[0].id.competenciaCodigo").value(1L));
    }

    @Test
    void listarPorAtividade_deveRetornarListaDeVinculos() throws Exception {
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);

        Competencia competencia = new Competencia();
        competencia.setCodigo(1L);

        CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(1L, 1L);
        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(atividade);
        vinculo.setCompetencia(competencia);

        when(repositorioCompetenciaAtividade.findAll()).thenReturn(Collections.singletonList(vinculo));

        mockMvc.perform(get("/api/competencia-atividades/por-atividade/1").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id.atividadeCodigo").value(1L));
    }

    @Test
    void vincular_comSucesso_deveCriarVinculo() throws Exception {
        CompetenciaAtividadeControlador.RequisicaoVinculo request = new CompetenciaAtividadeControlador.RequisicaoVinculo();
        request.setIdAtividade(1L);
        request.setIdCompetencia(1L);

        when(repositorioAtividade.findById(1L)).thenReturn(Optional.of(new Atividade()));
        when(repositorioCompetencia.findById(1L)).thenReturn(Optional.of(new Competencia()));
        when(repositorioCompetenciaAtividade.existsById(any())).thenReturn(false);
        when(repositorioCompetenciaAtividade.save(any())).thenReturn(new CompetenciaAtividade());

        mockMvc.perform(post("/api/competencia-atividades")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void vincular_quandoAtividadeNaoEncontrada_deveRetornarBadRequest() throws Exception {
        CompetenciaAtividadeControlador.RequisicaoVinculo request = new CompetenciaAtividadeControlador.RequisicaoVinculo();
        request.setIdAtividade(99L);
        request.setIdCompetencia(1L);

        when(repositorioAtividade.findById(99L)).thenReturn(Optional.empty());
        when(repositorioCompetencia.findById(1L)).thenReturn(Optional.of(new Competencia()));

        mockMvc.perform(post("/api/competencia-atividades")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vincular_quandoCompetenciaNaoEncontrada_deveRetornarBadRequest() throws Exception {
        CompetenciaAtividadeControlador.RequisicaoVinculo request = new CompetenciaAtividadeControlador.RequisicaoVinculo();
        request.setIdAtividade(1L);
        request.setIdCompetencia(99L);

        when(repositorioAtividade.findById(1L)).thenReturn(Optional.of(new Atividade()));
        when(repositorioCompetencia.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/competencia-atividades")
                        .with(user("testuser")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vincular_quandoVinculoJaExiste_deveRetornarConflict() throws Exception {
        CompetenciaAtividadeControlador.RequisicaoVinculo request = new CompetenciaAtividadeControlador.RequisicaoVinculo();
        request.setIdAtividade(1L);
        request.setIdCompetencia(1L);

        when(repositorioAtividade.findById(1L)).thenReturn(Optional.of(new Atividade()));
        when(repositorioCompetencia.findById(1L)).thenReturn(Optional.of(new Competencia()));
        when(repositorioCompetenciaAtividade.existsById(any())).thenReturn(true);

        mockMvc.perform(post("/api/competencia-atividades")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void desvincular_comSucesso_deveRemoverVinculo() throws Exception {
        when(repositorioCompetenciaAtividade.existsById(any())).thenReturn(true);
        doNothing().when(repositorioCompetenciaAtividade).deleteById(any());

        mockMvc.perform(delete("/api/competencia-atividades?idAtividade=1&idCompetencia=1")
                .with(user("testuser")).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void desvincular_quandoNaoEncontrado_deveRetornarNotFound() throws Exception {
        when(repositorioCompetenciaAtividade.existsById(any())).thenReturn(false);

        mockMvc.perform(delete("/api/competencia-atividades?idAtividade=1&idCompetencia=1")
                .with(user("testuser")).with(csrf()))
                .andExpect(status().isNotFound());
    }
}