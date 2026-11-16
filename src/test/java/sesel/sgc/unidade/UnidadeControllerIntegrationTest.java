package sesel.sgc.unidade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UnidadeControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @WithMockUser(username = "admin", roles = { "SEDOC" })
    @Test
    void deveRetornarOkNaListagemDeUnidades() throws Exception {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste %s".formatted(UUID.randomUUID()));
        unidade.setDescricao("Descricao Teste");
        unidadeRepository.save(unidade);

        mockMvc.perform(get("/unidade"))
                .andExpect(status().isOk())
                .andExpect(view().name("unidade/unidade-lista"));
    }

    @WithMockUser(username = "admin", roles = { "SEDOC" })
    @Test
    void deveRetornarOkNoFormularioDeNovaUnidade() throws Exception {
        mockMvc.perform(get("/unidade/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("unidade/unidade-form"));
    }
}