package sesel.sgc.atribuicao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class AtribuicaoViewControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @WithMockUser(username = "admin", roles = { "SEDOC" })
    @Test
    void deveRetornarOkNoFormularioAtribuicaoTemporaria() throws Exception {
        mockMvc.perform(get("/atribuicao/atribuicao-temporaria"))
                .andExpect(status().isOk())
                .andExpect(view().name("processo/atrib-temporaria"));
    }
}