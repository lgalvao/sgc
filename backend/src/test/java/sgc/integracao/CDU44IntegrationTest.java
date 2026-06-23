package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.integracao.mocks.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@DisplayName("CDU-44: Realizar autoavaliação")
class CDU44IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
    }

    @Test
    @WithMockCustomUser(tituloEleitoral = "50003", unidadeId = 9L, perfis = {"SERVIDOR"})
    @DisplayName("Deve salvar e concluir a autoavaliação do servidor")
    void deveSalvarEConcluirAutoavaliacao() throws Exception {
        mockMvc.perform(get(API_DIAGNOSTICO + "/autoavaliacao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(2))
                .andExpect(jsonPath("$.competencias[0].competenciaDescricao").isNotEmpty())
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

        mockMvc.perform(post(API_DIAGNOSTICO + "/autoavaliacao", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post(API_DIAGNOSTICO + "/autoavaliacao/concluir", subprocesso.getCodigo())
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
