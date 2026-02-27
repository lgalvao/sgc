package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.EmailService;
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
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - E-mail e Notificações")
class SubprocessoServiceEmailIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @MockitoBean
    private EmailService emailService;

    private Unidade unidade;
    private Unidade unidadeDestino;
    private Usuario admin;
    private Processo processo;
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

        processo = Processo.builder()
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
    @DisplayName("registrarTransicao: Lida com exceção ao enviar email sem quebrar a transação")
    void registrarTransicao_LidaComExcecaoEmail() {
        doThrow(new RuntimeException("Erro ao enviar email")).when(emailService).enviarEmailHtml(anyString(), anyString(), anyString());

        RegistrarTransicaoCommand comando = RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(unidade)
                .destino(unidadeDestino)
                .usuario(admin)
                .build();

        assertThatCode(() -> subprocessoService.registrarTransicao(comando)).doesNotThrowAnyException();
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
