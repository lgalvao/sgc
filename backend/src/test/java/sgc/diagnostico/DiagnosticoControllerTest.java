package sgc.diagnostico;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sgc.diagnostico.dto.AvaliacaoServidorDto;
import sgc.diagnostico.dto.ConcluirAutoavaliacaoRequest;
import sgc.diagnostico.dto.ConcluirDiagnosticoRequest;
import sgc.diagnostico.dto.DiagnosticoDto;
import sgc.diagnostico.dto.OcupacaoCriticaDto;
import sgc.diagnostico.dto.SalvarAvaliacaoRequest;
import sgc.diagnostico.dto.SalvarOcupacaoRequest;
import sgc.diagnostico.model.NivelAvaliacao;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.diagnostico.service.DiagnosticoService;
import sgc.integracao.mocks.TestSecurityConfig;

@WebMvcTest(DiagnosticoController.class)
@Import(TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class DiagnosticoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DiagnosticoService diagnosticoService;

    @Test
    @DisplayName("GET /api/diagnosticos/{id} - Deve retornar diagnostico")
    @WithMockUser
    void deveRetornarDiagnostico() throws Exception {
        Long id = 1L;
        when(diagnosticoService.buscarDiagnosticoCompleto(id)).thenReturn(DiagnosticoDto.builder().build());

        mockMvc.perform(get("/api/diagnosticos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /avaliacoes - Deve salvar avaliação")
    @WithMockUser
    void deveSalvarAvaliacao() throws Exception {
        Long id = 1L;
        SalvarAvaliacaoRequest request = new SalvarAvaliacaoRequest(10L, NivelAvaliacao.N1, NivelAvaliacao.N3, "Obs");

        when(diagnosticoService.salvarAvaliacao(eq(id), any(), any())).thenReturn(AvaliacaoServidorDto.builder().build());

        mockMvc.perform(post("/api/diagnosticos/{id}/avaliacoes", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /avaliacoes/minhas - Deve retornar avaliações")
    @WithMockUser
    void deveRetornarMinhasAvaliacoes() throws Exception {
        Long id = 1L;
        when(diagnosticoService.buscarMinhasAvaliacoes(eq(id), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/diagnosticos/{id}/avaliacoes/minhas", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /avaliacoes/concluir - Deve concluir autoavaliação")
    @WithMockUser
    void deveConcluirAutoavaliacao() throws Exception {
        Long id = 1L;
        ConcluirAutoavaliacaoRequest request = new ConcluirAutoavaliacaoRequest("Ok");

        mockMvc.perform(post("/api/diagnosticos/{id}/avaliacoes/concluir", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(diagnosticoService).concluirAutoavaliacao(eq(id), any(), any());
    }

    @Test
    @DisplayName("POST /ocupacoes - Deve salvar ocupação")
    @WithMockUser
    void deveSalvarOcupacao() throws Exception {
        Long id = 1L;
        SalvarOcupacaoRequest request = new SalvarOcupacaoRequest("123", 10L, SituacaoCapacitacao.AC);

        when(diagnosticoService.salvarOcupacao(eq(id), any())).thenReturn(OcupacaoCriticaDto.builder().build());

        mockMvc.perform(post("/api/diagnosticos/{id}/ocupacoes", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /ocupacoes - Deve retornar ocupações")
    @WithMockUser
    void deveRetornarOcupacoes() throws Exception {
        Long id = 1L;
        when(diagnosticoService.buscarOcupacoes(id)).thenReturn(List.of(OcupacaoCriticaDto.builder().build()));

        mockMvc.perform(get("/api/diagnosticos/{id}/ocupacoes", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /concluir - Deve concluir diagnostico")
    @WithMockUser
    void deveConcluirDiagnostico() throws Exception {
        Long id = 1L;
        ConcluirDiagnosticoRequest request = new ConcluirDiagnosticoRequest("Justificativa");

        when(diagnosticoService.buscarDiagnosticoCompleto(id)).thenReturn(DiagnosticoDto.builder().build());

        mockMvc.perform(post("/api/diagnosticos/{id}/concluir", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(diagnosticoService).concluirDiagnostico(eq(id), any());
    }
}
