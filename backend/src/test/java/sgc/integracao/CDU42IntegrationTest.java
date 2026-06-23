package sgc.integracao;

import org.junit.jupiter.api.*;
import sgc.integracao.mocks.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@DisplayName("CDU-42: Visualizar detalhes de subprocesso de diagnóstico para CHEFE e SERVIDOR")
class CDU42IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("CHEFE deve visualizar equipe da unidade com ações compatíveis com a situação dos servidores")
    void chefeDeveVisualizarEquipeDaUnidade() throws Exception {
        mockMvc.perform(get(API_DIAGNOSTICO + "/equipe", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servidores.length()").value(2))
                .andExpect(jsonPath("$.servidores[0].podeManterConsenso").value(true))
                .andExpect(jsonPath("$.servidores[0].podeImpossibilitar").value(true))
                .andExpect(jsonPath("$.servidores[0].podePermitirAvaliacao").value(false));
    }

    @Test
    @WithMockCustomUser(tituloEleitoral = "50003", unidadeId = 9L, perfis = {"SERVIDOR"})
    @DisplayName("SERVIDOR deve acessar a própria autoavaliação e não a visão completa da unidade")
    void servidorDeveAcessarAutoavaliacaoEManterRestricaoDaVisaoDaUnidade() throws Exception {
        mockMvc.perform(get(API_DIAGNOSTICO + "/autoavaliacao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(2))
                .andExpect(jsonPath("$.situacaoServidor").value("AUTOAVALIACAO_NAO_INICIADA"))
                .andExpect(jsonPath("$.podeEditar").value(true))
                .andExpect(jsonPath("$.habilitarConcluirAutoavaliacao").value(true));

        mockMvc.perform(get(API_DIAGNOSTICO + "/equipe", subprocesso.getCodigo()))
                .andExpect(status().isForbidden());
    }
}
