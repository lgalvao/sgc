package sgc.integracao;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import jakarta.mail.Address;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sgc.alerta.NotificacaoWorker;
import sgc.integracao.mocks.ColetorSqlTeste;
import sgc.integracao.mocks.TestConfig;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

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
    protected MovimentacaoRepo movimentacaoRepo;
    @Autowired
    protected UsuarioRepo usuarioRepo;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private CacheManager cacheManager;
    @Autowired(required = false)
    private NotificacaoWorker notificacaoWorker;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        ColetorSqlTeste.limpar();
        cacheManager.getCacheNames().forEach(nome -> {
            Cache cache = cacheManager.getCache(nome);
            if (cache != null) {
                cache.clear();
            }
        });
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
                .pollInSameThread()
                .untilAsserted(() -> {
                    processarEmailsPendentes();
                    assertThat(greenMail.getReceivedMessages()).hasSizeGreaterThanOrEqualTo(quantidade);
                });
    }

    protected void processarEmailsPendentes() {
        if (notificacaoWorker != null) {
            notificacaoWorker.processarPendentes();
        }
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

    protected boolean algumEmailComAssunto(String busca) throws Exception {
        if (greenMail == null) return false;
        var mensagens = greenMail.getReceivedMessages();
        for (var msg : mensagens) {
            String assunto = msg.getSubject();
            if (assunto != null && assunto.contains(busca)) {
                return true;
            }
        }
        return false;
    }

    protected boolean algumEmailPara(String destinatario) throws Exception {
        if (greenMail == null) return false;
        var mensagens = greenMail.getReceivedMessages();
        for (var msg : mensagens) {
            Address[] destinatarios = msg.getAllRecipients();
            if (destinatarios == null) {
                continue;
            }
            for (Address endereco : destinatarios) {
                if (endereco instanceof InternetAddress internetAddress
                        && destinatario.equalsIgnoreCase(internetAddress.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void registrarMovimentacaoInicial(Subprocesso subprocesso) {
        Usuario usuario = usuarioRepo.findById("111111111111").orElseThrow();
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(subprocesso.getUnidade())
                .unidadeDestino(subprocesso.getUnidade())
                .usuario(usuario)
                .descricao("Movimentação inicial de teste")
                .build());
    }

    protected String extrairHtmlDaMensagem(Part part) throws Exception {
        if ((part.isMimeType("text/html") || part.isMimeType("text/plain")) && part.getContent() instanceof String s) {
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
