package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.AtividadeRepo;
import sgc.integracao.mocks.TestConfig;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.UnidadeRepo;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestConfig.class)
public abstract class BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

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
