package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-49: Acompanhar diagnóstico de unidades subordinadas")
class CDU49IntegrationTest extends DiagnosticoCduIntegrationTestBase {

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherAutoavaliacao("50003", 5, 4, 4, 3, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @WithMockGestor("202020202020")
    @DisplayName("GESTOR da unidade superior deve consultar o diagnóstico da unidade subordinada")
    void gestorDeveConsultarDiagnosticoDaUnidadeSubordinada() throws Exception {
        mockMvc.perform(get("/api/diagnosticos/subprocessos/{codSubprocesso}/unidade", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.unidadeSigla").value("SEDIA"))
                .andExpect(jsonPath("$.unidade.situacaoSubprocesso").value("DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO"))
                .andExpect(jsonPath("$.situacaoDiagnostico").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.servidores.length()").value(2))
                .andExpect(jsonPath("$.servidores[0].servidorTitulo").exists())
                .andExpect(jsonPath("$.ocupacoesCriticas.length()").value(4));
    }

    @Test
    @WithMockAdmin
    @DisplayName("ADMIN deve consultar o diagnóstico completo da unidade")
    void adminDeveConsultarDiagnosticoCompletoDaUnidade() throws Exception {
        mockMvc.perform(get("/api/diagnosticos/subprocessos/{codSubprocesso}/unidade", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.unidadeSigla").value("SEDIA"))
                .andExpect(jsonPath("$.unidade.situacaoSubprocesso").value("DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO"))
                .andExpect(jsonPath("$.situacaoDiagnostico").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.servidores.length()").value(2))
                .andExpect(jsonPath("$.ocupacoesCriticas.length()").value(4))
                .andExpect(jsonPath("$.movimentacoes").isArray());
    }
}
