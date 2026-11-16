package sesel.sgc.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarViewLoginSemParametros() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void deveRetornarViewLoginComErro() throws Exception {
        mockMvc.perform(get("/login?error=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void deveRetornarViewLoginComLogout() throws Exception {
        mockMvc.perform(get("/login?logout=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void deveRetornarViewAcessoNegado() throws Exception {
        mockMvc.perform(get("/acesso-negado"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }
}