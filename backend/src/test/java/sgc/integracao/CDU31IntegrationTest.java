package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.integracao.mocks.*;
import sgc.parametros.model.*;
import tools.jackson.databind.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
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
