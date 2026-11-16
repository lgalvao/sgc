package sesel.sgc.servidor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.seguranca.Perfil;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeRepository;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ServidorControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Autowired
    private ServidorRepository servidorRepository;

    @WithMockUser(username = "admin", roles = {"SEDOC"})
    @Transactional
    @Test
    void deveRetornarOkNaListagemDeServidoresPorUnidade() throws Exception {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste " + UUID.randomUUID());
        unidade.setDescricao("Descricao Teste");
        unidade = unidadeRepository.save(unidade);

        Servidor servidor = new Servidor();
        servidor.setNome("Servidor Teste");
        servidor.setEmail("servidor%s@teste.com".formatted(UUID.randomUUID()));
        servidor.setSenha("123456");
        servidor.setCargo("Analista");
        servidor.setUnidade(unidade);
        servidor.setPerfis(List.of(Perfil.SERVIDOR));
        servidorRepository.save(servidor);

        mockMvc.perform(get("/servidores/unidade/%s".formatted(unidade.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("servidor/servidor-lista"));
    }
}