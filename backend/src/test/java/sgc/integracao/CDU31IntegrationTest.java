package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import sgc.configuracao.model.Parametro;
import sgc.configuracao.model.ParametroRepo;
import sgc.integracao.mocks.WithMockAdmin;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@Transactional
@DisplayName("CDU-31: Configurar sistema")
class CDU31IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParametroRepo parametroRepo;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Parametro p1 = new Parametro();
        p1.setChave("DIAS_INATIVACAO_PROCESSO");
        p1.setValor("30");
        p1.setDescricao("Dias para inativação");
        parametroRepo.save(p1);

        Parametro p2 = new Parametro();
        p2.setChave("DIAS_ALERTA_NOVO");
        p2.setValor("5");
        p2.setDescricao("Dias alerta novo");
        parametroRepo.save(p2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve permitir listar e atualizar configurações como ADMIN")
    @WithMockAdmin
    void atualizarConfiguracoes_sucesso() throws Exception {
        // Listar
        mockMvc.perform(get("/api/configuracoes")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("DIAS_INATIVACAO_PROCESSO");
                    assertThat(content).contains("30");
                });

        // Atualizar
        List<Parametro> existing = parametroRepo.findAll();
        existing.forEach(p -> {
            if (p.getChave().equals("DIAS_INATIVACAO_PROCESSO")) p.setValor("60");
            if (p.getChave().equals("DIAS_ALERTA_NOVO")) p.setValor("10");
        });

        mockMvc.perform(post("/api/configuracoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existing)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Parametro atualizado = parametroRepo.findByChave("DIAS_INATIVACAO_PROCESSO").orElseThrow();
        assertThat(atualizado.getValor()).isEqualTo("60");
    }
}
