package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sgc.atividade.model.AtividadeRepo;
import sgc.integracao.mocks.TestConfig;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.UnidadeRepo;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@Transactional
@Import(TestConfig.class)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ProcessoRepo processoRepo;

    @Autowired
    protected SubprocessoRepo subprocessoRepo;

    @Autowired
    protected UnidadeRepo unidadeRepo;

    @Autowired
    protected AtividadeRepo atividadeRepo;

    @Autowired
    protected MapaRepo mapaRepo;
}
