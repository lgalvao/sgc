package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.mail.javamail.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.fixture.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - E-mail e Notificações")
class SubprocessoServiceEmailIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoTransicaoService transicaoService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private JavaMailSenderImpl javaMailSender;

    private Unidade unidade;
    private Unidade unidadeDestino;
    private Usuario admin;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        when(javaMailSender.createMimeMessage()).thenAnswer(invocacao -> new jakarta.mail.internet.MimeMessage(jakarta.mail.Session.getInstance(new Properties())));

        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_EMAIL_ORIG");
        unidade.setNome("Unidade email origem");
        unidade = unidadeRepo.save(unidade);

        unidadeDestino = UnidadeFixture.unidadePadrao();
        unidadeDestino.setCodigo(null);
        unidadeDestino.setSigla("TEST_EMAIL_DEST");
        unidadeDestino.setNome("Unidade email destino");
        unidadeDestino = unidadeRepo.save(unidadeDestino);

        admin = usuarioRepo.findById("111111111111").orElseThrow();

        jdbcTemplate.update(
                "INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidade.getCodigo(), admin.getTituloEleitoral(), admin.getMatricula(), "TITULAR", LocalDateTime.now()
        );

        jdbcTemplate.update(
                "INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidadeDestino.getCodigo(), admin.getTituloEleitoral(), admin.getMatricula(), "TITULAR", LocalDateTime.now()
        );

        Processo processo = Processo.builder()
                .descricao("Processo teste email")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("registrarTransicao: Deve notificar unidade destino ao disponibilizar cadastro")
    void registrarTransicao_EnviaEmailDestino() {
        RegistrarTransicaoCommand comando = RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DEVOLVIDO)
                .origem(unidade)
                .destino(unidadeDestino)
                .usuario(admin)
                .observacoes("Testando email")
                .build();

        transicaoService.registrarTransicao(comando);

        verify(javaMailSender, atLeastOnce()).send(any(jakarta.mail.internet.MimeMessage.class));
    }

    @Test
    @DisplayName("registrarTransicao: Deve tentar enviar email ao disponibilizar cadastro")
    void registrarTransicao_TentaEnviarEmailAoDisponibilizarCadastro() {
        RegistrarTransicaoCommand comando = RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(unidade)
                .destino(unidadeDestino)
                .usuario(admin)
                .build();

        transicaoService.registrarTransicao(comando);

        verify(javaMailSender, atLeastOnce()).send(any(jakarta.mail.internet.MimeMessage.class));
    }

    @Test
    @DisplayName("registrarTransicao: Não envia e-mail caso tipo de transição não exija")
    void registrarTransicao_NaoEnviaEmail() {
        RegistrarTransicaoCommand comando = RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .origem(unidade)
                .destino(unidadeDestino)
                .usuario(admin)
                .build();

        transicaoService.registrarTransicao(comando);

        verify(javaMailSender, never()).send(any(jakarta.mail.internet.MimeMessage.class));
    }
}
