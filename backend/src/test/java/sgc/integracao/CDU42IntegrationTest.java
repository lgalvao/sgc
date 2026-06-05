package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sgc.diagnostico.dto.AutoavaliacaoRequest;
import sgc.diagnostico.dto.AvaliacaoCompetenciaDto;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.integracao.mocks.WithMockCustomUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-42: Realizar autoavaliação")
class CDU42IntegrationTest extends DiagnosticoCduIntegrationTestBase {

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
    }

    @Test
    @WithMockCustomUser(tituloEleitoral = "50003", unidadeId = 9L, perfis = {"SERVIDOR"})
    @DisplayName("Deve salvar e concluir a autoavaliação do servidor")
    void deveSalvarEConcluirAutoavaliacao() throws Exception {
        mockMvc.perform(get("/api/diagnosticos/subprocessos/{codSubprocesso}/autoavaliacao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(2))
                .andExpect(jsonPath("$.situacaoServidor").value("AUTOAVALIACAO_NAO_INICIADA"));

        AutoavaliacaoRequest request = new AutoavaliacaoRequest(List.of(
                AvaliacaoCompetenciaDto.builder()
                        .competenciaCodigo(competencia1.getCodigo())
                        .importancia(5)
                        .dominio(3)
                        .build(),
                AvaliacaoCompetenciaDto.builder()
                        .competenciaCodigo(competencia2.getCodigo())
                        .importancia(4)
                        .dominio(2)
                        .build()
        ));

        mockMvc.perform(post("/api/diagnosticos/subprocessos/{codSubprocesso}/autoavaliacao", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/diagnosticos/subprocessos/{codSubprocesso}/autoavaliacao/concluir", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).hasSize(2);
        assertThat(avaliacoes).allSatisfy(avaliacao -> {
            assertThat(avaliacao.getAutoimportancia()).isNotNull();
            assertThat(avaliacao.getAutodominio()).isNotNull();
            assertThat(avaliacao.getImportancia()).isNotNull();
            assertThat(avaliacao.getDominio()).isNotNull();
            assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        });
    }
}
