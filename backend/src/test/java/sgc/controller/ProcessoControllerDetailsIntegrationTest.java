package sgc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Integration tests para o endpoint GET /api/processos/{id}/detalhes
 * - Usa H2 e carrega dados de backend/src/main/resources/data.sql via @Sql
 * - Testa caso feliz (ADMIN) e cenário de autorização negativa (GESTOR)
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProcessoControllerDetailsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void detalhesProcesso_Admin_Ok() throws Exception {
        mockMvc.perform(get("/api/processos/1/detalhes")
                        .param("perfil", "ADMIN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(1))
                .andExpect(jsonPath("$..sigla", hasItem("DEX")))
                .andExpect(jsonPath("$.resumoSubprocessos").isArray());
    }

    @Test
    public void detalhesProcesso_Gestor_SemPermissao_Forbidden() throws Exception {
        // No fixture, o subprocesso do processo 1 está associado à unidade 10 (DEX).
        // Um gestor da unidade 11 não deve ter acesso.
        mockMvc.perform(get("/api/processos/1/detalhes")
                        .param("perfil", "GESTOR")
                        .param("unidade", "11")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}