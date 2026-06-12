package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sgc.diagnostico.dto.SituacaoCapacitacaoDto;
import sgc.diagnostico.dto.SituacoesCapacitacaoRequest;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.diagnostico.model.ValorSituacaoCapacitacao;
import sgc.integracao.mocks.WithMockChefe;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-48: Preencher situações de capacitação")
class CDU48IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve salvar a situação de capacitação das ocupações críticas")
    void deveSalvarSituacaoCapacitacao() throws Exception {
        SituacoesCapacitacaoRequest request = new SituacoesCapacitacaoRequest(List.of(
                SituacaoCapacitacaoDto.builder()
                        .competenciaCodigo(competencia1.getCodigo())
                        .servidorTitulo("50003")
                        .situacaoCapacitacao(ValorSituacaoCapacitacao.AC.name())
                        .build(),
                SituacaoCapacitacaoDto.builder()
                        .competenciaCodigo(competencia2.getCodigo())
                        .servidorTitulo("50003")
                        .situacaoCapacitacao(ValorSituacaoCapacitacao.C.name())
                        .build()
        ));

        mockMvc.perform(post(API_DIAGNOSTICO + "/situacoes-capacitacao", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<SituacaoCapacitacao> ocupacoes = buscarSituacoesCapacitacao("50003");
        assertThat(ocupacoes).hasSize(2);
        assertThat(ocupacoes).anySatisfy(ocupacao -> {
            if (ocupacao.getCompetencia().getCodigo().equals(competencia1.getCodigo())) {
                assertThat(ocupacao.getSituacaoCapacitacao()).isEqualTo(ValorSituacaoCapacitacao.AC);
            }
        });
        assertThat(ocupacoes).anySatisfy(ocupacao -> {
            if (ocupacao.getCompetencia().getCodigo().equals(competencia2.getCodigo())) {
                assertThat(ocupacao.getSituacaoCapacitacao()).isEqualTo(ValorSituacaoCapacitacao.C);
            }
        });
    }
}
