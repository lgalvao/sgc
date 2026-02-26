package sgc.integracao;

import com.icegreen.greenmail.store.*;
import com.icegreen.greenmail.util.*;
import jakarta.mail.*;
import org.awaitility.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.context.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import tools.jackson.databind.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

@SpringBootTest
@Transactional
@Import({TestConfig.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@Tag("integration")
public abstract class BaseIntegrationTest {
    protected MockMvc mockMvc;
    
    @Autowired(required = false)
    protected GreenMail greenMail;

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
            try {
                greenMail.purgeEmailFromAllMailboxes();
            } catch (FolderException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void aguardarEmail(int quantidade) {
        if (greenMail == null) return;
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> 
                    assertThat(greenMail.getReceivedMessages()).hasSizeGreaterThanOrEqualTo(quantidade)
                );
    }

    protected boolean algumEmailContem(String busca) throws Exception {
        if (greenMail == null) return false;
        var mensagens = greenMail.getReceivedMessages();
        for (var msg : mensagens) {
            String html = extrairHtmlDaMensagem(msg);
            if (html != null && html.contains(busca)) {
                return true;
            }
        }
        return false;
    }

    private String extrairHtmlDaMensagem(Part part) throws Exception {
        if (part.isMimeType("text/html") && part.getContent() instanceof String s) {
            return s;
        }
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = extrairHtmlDaMensagem(mp.getBodyPart(i));
                if (s != null) return s;
            }
        }
        return null;
    }
}
