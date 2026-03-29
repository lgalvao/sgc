package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.alerta.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoNotificacaoService")
@SuppressWarnings("NullAway.Init")
class SubprocessoNotificacaoServiceTest {

    @Mock
    private AlertaFacade alertaService;
    @Mock
    private EmailService emailService;
    @Mock
    private ResponsavelUnidadeService responsavelService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private SubprocessoNotificacaoService service;

    @Captor
    private ArgumentCaptor<String> templateCaptor;
    @Captor
    private ArgumentCaptor<IContext> contextCaptor;

    @Test
    @DisplayName("deve criar alerta e enviar email direto e para substituto")
    void deveCriarAlertaEEnviarEmailDiretoEParaSubstituto() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
        Subprocesso subprocesso = criarSubprocesso(origem, processo);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.of(2026, 4, 10, 9, 0));
        subprocesso.setDataLimiteEtapa2(LocalDateTime.of(2026, 4, 20, 17, 0));

        when(responsavelService.buscarResponsavelUnidade(destino.getCodigo()))
                .thenReturn(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .substitutoTitulo("substituto")
                        .substitutoNome("Substituto")
                        .build());
        when(usuarioService.buscarOpt("substituto"))
                .thenReturn(Optional.of(criarUsuario("substituto@tre-pe.jus.br")));

        service.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.PROCESSO_INICIADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .observacoes("Observacao importante")
                .build());

        verify(alertaService).criarAlertaTransicao(
                eq(processo),
                eq(TipoTransicao.PROCESSO_INICIADO.formatarAlerta("ORIG")),
                eq(origem),
                eq(destino));
        verify(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), startsWith("SGC: "), eq("<html>corpo</html>"));
        verify(emailService).enviarEmailHtml(eq("substituto@tre-pe.jus.br"), startsWith("SGC: "), eq("<html>corpo</html>"));
        verify(templateEngine).process(templateCaptor.capture(), contextCaptor.capture());

        assertThat(templateCaptor.getValue()).isEqualTo("processo-iniciado");
        IContext contexto = contextCaptor.getValue();
        assertThat(contexto.getVariable("siglaUnidade")).isEqualTo("ORIG");
        assertThat(contexto.getVariable("siglaUnidadeDestino")).isEqualTo("DEST");
        assertThat(contexto.getVariable("nomeProcesso")).isEqualTo("Processo teste");
        assertThat(contexto.getVariable("dataLimiteEtapa1")).isEqualTo("10/04/2026");
        assertThat(contexto.getVariable("dataLimiteEtapa2")).isEqualTo("20/04/2026");
        assertThat(contexto.getVariable("dataLimiteValidacao")).isEqualTo("20/04/2026");
        assertThat(contexto.getVariable("observacoes")).isEqualTo("Observacao importante");
    }

    @Test
    @DisplayName("deve notificar superiores ignorando destino repetido na hierarquia")
    void deveNotificarSuperioresIgnorandoDestinoRepetidoNaHierarquia() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade superiorFinal = criarUnidade(30L, "SUP2", "Superior final");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        destino.setUnidadeSuperior(superiorFinal);

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        origem.setUnidadeSuperior(destino);

        Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        when(responsavelService.buscarResponsavelUnidade(destino.getCodigo()))
                .thenReturn(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build());

        service.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), startsWith("SGC: "), eq("<html>corpo</html>"));
        verify(emailService).enviarEmailHtml(eq("sup2@tre-pe.jus.br"), contains(" - ORIG"), eq("<html>corpo</html>"));
        verify(emailService, times(2)).enviarEmailHtml(anyString(), anyString(), anyString());

        verify(templateEngine, times(2)).process(templateCaptor.capture(), contextCaptor.capture());
        assertThat(templateCaptor.getAllValues()).containsExactly("cadastro-devolvido", "cadastro-devolvido-superior");
        assertThat(contextCaptor.getAllValues().get(1).getVariable("siglaUnidadeSuperior")).isEqualTo("SUP2");
    }

    @Test
    @DisplayName("nao deve enviar email pessoal quando substituto nao possuir email")
    void naoDeveEnviarEmailPessoalQuandoSubstitutoNaoPossuirEmail() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        when(responsavelService.buscarResponsavelUnidade(destino.getCodigo()))
                .thenReturn(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .substitutoTitulo("substituto")
                        .substitutoNome("Substituto")
                        .build());
        when(usuarioService.buscarOpt("substituto"))
                .thenReturn(Optional.of(criarUsuario("")));

        service.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.PROCESSO_INICIADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(emailService, times(1)).enviarEmailHtml(anyString(), anyString(), anyString());
        verify(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), anyString(), anyString());
    }

    @Test
    @DisplayName("nao deve acionar alerta ou email quando a transicao nao exigir notificacao")
    void naoDeveAcionarAlertaOuEmailQuandoATransicaoNaoExigirNotificacao() {
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        service.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_HOMOLOGADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verifyNoInteractions(alertaService, emailService, responsavelService, usuarioService, templateEngine);
    }

    @Test
    @DisplayName("deve ignorar envio de email quando tipo nao enviar email ao chamar rotina interna")
    void deveIgnorarEnvioDeEmailQuandoTipoNaoEnviaEmail() {
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso(TipoProcesso.MAPEAMENTO);
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        invokeMethod(service, "notificarMovimentacaoEmail", NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_HOMOLOGADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verifyNoInteractions(emailService, responsavelService, usuarioService, templateEngine);
    }

    @Test
    @DisplayName("deve lançar erro quando template direto estiver ausente")
    void deveLancarErroQuandoTemplateDiretoAusente() {
        assertThatThrownBy(() -> invokeMethod(service, "obterTemplateObrigatorio", " ", "e-mail direto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template ausente");
    }

    @Test
    @DisplayName("deve lançar erro quando template direto for nulo")
    void deveLancarErroQuandoTemplateDiretoForNulo() {
        assertThatThrownBy(() -> invokeMethod(service, "obterTemplateObrigatorio", null, "e-mail direto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template ausente");
    }

    @Test
    @DisplayName("getEmailUnidade deve gerar endereco com sigla minuscula")
    void getEmailUnidadeDeveGerarEnderecoComSiglaMinuscula() {
        assertThat(service.getEmailUnidade(criarUnidade(1L, "ABC", "Unidade"))).isEqualTo("abc@tre-pe.jus.br");
    }

    private Processo criarProcesso(TipoProcesso tipo) {
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        processo.setTipo(tipo);
        return processo;
    }

    private Subprocesso criarSubprocesso(Unidade unidade, Processo processo) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(processo);
        return subprocesso;
    }

    private Unidade criarUnidade(Long codigo, String sigla, String nome) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        return unidade;
    }

    private Usuario criarUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        return usuario;
    }
}
