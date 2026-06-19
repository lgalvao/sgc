package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.WithMockAdmin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("ValidadorDadosOrganizacionais — integração")
class ValidadorDadosOrganizacionaisIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Deve retornar diagnóstico organizacional para ADMIN")
    @WithMockAdmin
    void deveRetornarDiagnosticoOrganizacional() throws Exception {
        mockMvc.perform(get("/api/unidades/diagnostico-organizacional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possuiViolacoes").exists())
                .andExpect(jsonPath("$.resumo").exists())
                .andExpect(jsonPath("$.grupos").isArray())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isNotBlank());
    }
}
