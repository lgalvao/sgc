package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.transaction.annotation.*;
import sgc.integracao.mocks.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
