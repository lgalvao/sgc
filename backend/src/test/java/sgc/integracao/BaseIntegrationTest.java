package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.integracao.mocks.TestConfig;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.UnidadeRepo;

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
