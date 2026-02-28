package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - E-mail e Notificações")
class SubprocessoServiceEmailIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EmailService emailService;

    private Unidade unidade;
    private Unidade unidadeDestino;
    private Usuario admin;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_EMAIL_ORIG");
        unidade.setNome("Unidade Email Origem");
        unidade = unidadeRepo.save(unidade);

        unidadeDestino = UnidadeFixture.unidadePadrao();
        unidadeDestino.setCodigo(null);
        unidadeDestino.setSigla("TEST_EMAIL_DEST");
        unidadeDestino.setNome("Unidade Email Destino");
        unidadeDestino = unidadeRepo.save(unidadeDestino);

        admin = usuarioRepo.findById("111111111111").orElseThrow();

        // Setup Responsabilidade for units using JDBC to bypass Hibernate @Immutable / null identifier issues
        jdbcTemplate.update(
                "INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidade.getCodigo(), admin.getTituloEleitoral(), admin.getMatricula(), "TITULAR", LocalDateTime.now()
        );

        jdbcTemplate.update(
                "INSERT INTO sgc.vw_responsabilidade (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidadeDestino.getCodigo(), admin.getTituloEleitoral(), admin.getMatricula(), "TITULAR", LocalDateTime.now()
        );

        Processo processo = Processo.builder()
                .descricao("Processo Teste Email")
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

        subprocessoService.registrarTransicao(comando);

        verify(emailService, atLeastOnce()).enviarEmailHtml(contains("test_email_dest"), anyString(), anyString());
    }

    @Test
    @DisplayName("registrarTransicao: Lança exceção ao falhar envio de email (try-catch removido)")
    void registrarTransicao_LancaExcecaoEmail() {
        doThrow(new RuntimeException("Erro ao enviar email")).when(emailService).enviarEmailHtml(anyString(), anyString(), anyString());

        RegistrarTransicaoCommand comando = RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(unidade)
                .destino(unidadeDestino)
                .usuario(admin)
                .build();

        assertThatThrownBy(() -> subprocessoService.registrarTransicao(comando))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao enviar email");
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

        subprocessoService.registrarTransicao(comando);

        verify(emailService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
    }
}
