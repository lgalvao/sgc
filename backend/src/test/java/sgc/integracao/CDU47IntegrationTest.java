package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sgc.diagnostico.dto.OcupacaoCriticaDto;
import sgc.diagnostico.dto.OcupacoesCriticasRequest;
import sgc.diagnostico.model.OcupacaoCritica;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.integracao.mocks.WithMockChefe;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-47: Preencher situação de capacitação")
class CDU47IntegrationTest extends DiagnosticoCduIntegrationTestBase {

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve salvar a situação de capacitação das ocupações críticas")
    void deveSalvarSituacaoCapacitacao() throws Exception {
        OcupacoesCriticasRequest request = new OcupacoesCriticasRequest(List.of(
                OcupacaoCriticaDto.builder()
                        .competenciaCodigo(competencia1.getCodigo())
                        .servidorTitulo("50003")
                        .situacaoCapacitacao(SituacaoCapacitacao.AC.name())
                        .build(),
                OcupacaoCriticaDto.builder()
                        .competenciaCodigo(competencia2.getCodigo())
                        .servidorTitulo("50003")
                        .situacaoCapacitacao(SituacaoCapacitacao.C.name())
                        .build()
        ));

        mockMvc.perform(post("/api/diagnosticos/subprocessos/{codSubprocesso}/ocupacoes-criticas", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<OcupacaoCritica> ocupacoes = buscarOcupacoes("50003");
        assertThat(ocupacoes).hasSize(2);
        assertThat(ocupacoes).anySatisfy(ocupacao -> {
            if (ocupacao.getCompetencia().getCodigo().equals(competencia1.getCodigo())) {
                assertThat(ocupacao.getSituacaoCapacitacao()).isEqualTo(SituacaoCapacitacao.AC);
            }
        });
        assertThat(ocupacoes).anySatisfy(ocupacao -> {
            if (ocupacao.getCompetencia().getCodigo().equals(competencia2.getCodigo())) {
                assertThat(ocupacao.getSituacaoCapacitacao()).isEqualTo(SituacaoCapacitacao.C);
            }
        });
    }
}
