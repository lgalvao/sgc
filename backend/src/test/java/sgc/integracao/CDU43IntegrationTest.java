package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.integracao.mocks.WithMockChefe;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-43: Acompanhar diagnóstico da unidade")
class CDU43IntegrationTest extends DiagnosticoCduIntegrationTestBase {

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherAutoavaliacao("50003", 5, 3, 4, 2, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve listar a equipe com a situação atual de cada servidor")
    void deveListarEquipeComSituacoes() throws Exception {
        mockMvc.perform(get("/api/diagnosticos/subprocessos/{codSubprocesso}/equipe", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servidores.length()").value(2))
                .andExpect(jsonPath("$.servidores[0].servidorTitulo").exists())
                .andExpect(jsonPath("$.servidores[1].servidorTitulo").exists())
                .andExpect(jsonPath("$.servidores[*].situacaoServidor").isArray());
    }
}
