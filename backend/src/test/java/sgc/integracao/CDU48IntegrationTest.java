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

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve criar registro de situação de capacitação quando ainda não houver registro persistido")
    void deveCriarSituacaoCapacitacaoQuandoRegistroNaoExistir() throws Exception {
        preencherConsenso("50003", 4, 4, 4, 4, 3, 3, 3, 3, sgc.diagnostico.model.SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        situacaoCapacitacaoRepo.deleteAll(buscarSituacoesCapacitacao("50003"));
        situacaoCapacitacaoRepo.flush();

        SituacoesCapacitacaoRequest request = new SituacoesCapacitacaoRequest(List.of(
                SituacaoCapacitacaoDto.builder()
                        .competenciaCodigo(competencia1.getCodigo())
                        .servidorTitulo("50003")
                        .situacaoCapacitacao(ValorSituacaoCapacitacao.EC.name())
                        .build()
        ));

        mockMvc.perform(post(API_DIAGNOSTICO + "/situacoes-capacitacao", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<SituacaoCapacitacao> ocupacoes = buscarSituacoesCapacitacao("50003");
        assertThat(ocupacoes).singleElement().satisfies(ocupacao -> {
            assertThat(ocupacao.getCompetencia().getCodigo()).isEqualTo(competencia1.getCodigo());
            assertThat(ocupacao.getSituacaoCapacitacao()).isEqualTo(ValorSituacaoCapacitacao.EC);
            assertThat(ocupacao.getUnidadeSiglaSnapshot()).isEqualTo(unidade.getSigla());
        });
    }
}
