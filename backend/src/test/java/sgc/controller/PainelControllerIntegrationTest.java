package sgc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PainelControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarProcessos_casoFeliz_usuario_retornaDoisProcessos() throws Exception {
        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "USER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].codigo").exists())
                .andExpect(jsonPath("$.content[0].descricao").exists());
    }

    @Test
    void listarProcessos_admin_somenteCriado() throws Exception {
        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "ADMIN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].situacao", is("CRIADO")));
    }

    @Test
    void listarProcessos_filtraPorUnidade_semResultados() throws Exception {
        // Unidade inexistente (9999) -> espera 0 resultados
        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "USER")
                        .param("unidade", "9999")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void listarAlertas_paraUsuario() throws Exception {
        mockMvc.perform(get("/api/painel/alertas")
                        .param("usuarioTitulo", "USR100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].usuarioDestinoTitulo", is("USR100")))
                .andExpect(jsonPath("$.content[0].descricao", containsString("Alerta")));
    }
}