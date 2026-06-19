package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoTransicao;
import sgc.subprocesso.service.SubprocessoTransicaoService;

import java.time.LocalDateTime;
import java.util.Properties;

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
                "INSERT INTO sgc.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidade.getCodigo(), admin.getTituloEleitoral(), admin.getMatricula(), "TITULAR", LocalDateTime.now()
        );

        jdbcTemplate.update(
                "INSERT INTO sgc.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
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
        processarEmailsPendentes();

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
        processarEmailsPendentes();

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
