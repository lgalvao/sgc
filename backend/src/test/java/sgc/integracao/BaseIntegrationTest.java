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
import sgc.integracao.mocks.TestConfig;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@Transactional
@Import({TestConfig.class, sgc.integracao.config.EmailTestConfig.class})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles({"test", "email-test"})
public abstract class BaseIntegrationTest {
    protected MockMvc mockMvc;
    
    @Autowired(required = false)
    protected com.icegreen.greenmail.util.GreenMail greenMail;
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
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        if (greenMail != null) {
            greenMail.reset();
        }
    }

    protected boolean algumEmailContem(String busca) throws Exception {
        var mensagens = greenMail.getReceivedMessages();
        for (var msg : mensagens) {
            String html = extrairHtmlDaMensagem(msg);
            if (html != null && html.contains(busca)) {
                return true;
            }
        }
        return false;
    }

    private String extrairHtmlDaMensagem(jakarta.mail.Part part) throws Exception {
        if (part.isMimeType("text/html") && part.getContent() instanceof String s) {
            return s;
        }
        if (part.isMimeType("multipart/*")) {
            jakarta.mail.Multipart mp = (jakarta.mail.Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = extrairHtmlDaMensagem(mp.getBodyPart(i));
                if (s != null) return s;
            }
        }
        return null;
    }
}
