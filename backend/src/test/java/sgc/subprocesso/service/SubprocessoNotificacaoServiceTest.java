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

import java.lang.reflect.*;
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
    private NotificacaoEmailService notificacaoEmailService;
    @Mock
    private ResponsavelUnidadeService responsavelService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private SpringTemplateEngine templateEngine;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private SubprocessoNotificacaoService service;

    @Captor
    private ArgumentCaptor<String> templateCaptor;
    @Captor
    private ArgumentCaptor<IContext> contextCaptor;
    @Captor
    private ArgumentCaptor<EnfileirarNotificacaoEmailCommand> notificacaoEmailCaptor;

    @Test
    @DisplayName("deve criar alerta e enviar email direto e para substituto")
    void deveCriarAlertaEEnviarEmailDiretoEParaSubstituto() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
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
        verify(notificacaoEmailService, times(2)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoEmailCommand::destinatario)
                .containsExactly("dest@tre-pe.jus.br", "substituto@tre-pe.jus.br");
        assertThat(notificacaoEmailCaptor.getAllValues())
                .allSatisfy(cmd -> {
                    assertThat(cmd.assunto()).startsWith("SGC: ");
                    assertThat(cmd.corpoHtml()).isEqualTo("<html>corpo</html>");
                    assertThat(cmd.tipoNotificacao()).isEqualTo(TipoTransicao.PROCESSO_INICIADO.name());
                    assertThat(cmd.chaveIdempotencia()).contains("transicao:PROCESSO_INICIADO");
                });
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
    @DisplayName("deve notificar apenas o superior imediato quando diferente do destino")
    void deveNotificarApenasSuperiorImediatoQuandoDiferenteDoDestino() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade superiorImediato = criarUnidade(20L, "SUP1", "Superior imediato");
        Unidade destino = criarUnidade(30L, "DEST", "Unidade destino");
        destino.setUnidadeSuperior(superiorImediato);

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        origem.setUnidadeSuperior(superiorImediato);

        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        when(responsavelService.buscarResponsavelUnidade(destino.getCodigo()))
                .thenReturn(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build());
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(20L);
        when(unidadeService.buscarResumosPorCodigos(List.of(20L))).thenReturn(List.of(
                new UnidadeResumoLeitura(20L, "Superior imediato", "SUP1", TipoUnidade.INTERMEDIARIA)
        ));

        service.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(notificacaoEmailService, times(2)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoEmailCommand::destinatario)
                .containsExactly("dest@tre-pe.jus.br", "sup1@tre-pe.jus.br");
        assertThat(notificacaoEmailCaptor.getAllValues().get(1).assunto()).contains(" - ORIG");

        verify(templateEngine, times(2)).process(templateCaptor.capture(), contextCaptor.capture());
        assertThat(templateCaptor.getAllValues()).containsExactly("cadastro-devolvido", "cadastro-devolvido-superior");
        assertThat(contextCaptor.getAllValues().get(1).getVariable("siglaUnidadeSuperior")).isEqualTo("SUP1");
    }

    @Test
    @DisplayName("nao deve reenviar email superior quando o destino ja for o superior imediato")
    void naoDeveReenviarEmailSuperiorQuandoDestinoJaForSuperiorImediato() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        origem.setUnidadeSuperior(destino);

        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        when(responsavelService.buscarResponsavelUnidade(destino.getCodigo()))
                .thenReturn(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build());
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(20L);

        service.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(notificacaoEmailService, times(1)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().destinatario()).isEqualTo("dest@tre-pe.jus.br");
        verify(unidadeService, never()).buscarResumosPorCodigos(anyList());
    }

    @Test
    @DisplayName("nao deve enviar email pessoal quando substituto nao possuir email")
    void naoDeveEnviarEmailPessoalQuandoSubstitutoNaoPossuirEmail() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
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

        verify(notificacaoEmailService, times(1)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().destinatario()).isEqualTo("dest@tre-pe.jus.br");
    }

    @Test
    @DisplayName("nao deve acionar alerta ou email quando a transicao nao exigir notificacao")
    void naoDeveAcionarAlertaOuEmailQuandoATransicaoNaoExigirNotificacao() {
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        service.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_HOMOLOGADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verifyNoInteractions(alertaService, notificacaoEmailService, responsavelService, usuarioService, templateEngine);
    }

    @Test
    @DisplayName("deve ignorar envio de email quando tipo nao enviar email ao chamar rotina interna")
    void deveIgnorarEnvioDeEmailQuandoTipoNaoEnviaEmail() {
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        invokeMethod(service, "notificarMovimentacaoEmail", NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_HOMOLOGADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verifyNoInteractions(notificacaoEmailService, responsavelService, usuarioService, templateEngine);
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
    void deveLancarErroQuandoTemplateDiretoForNulo() throws Exception {
        Method metodo = SubprocessoNotificacaoService.class.getDeclaredMethod("obterTemplateObrigatorio", String.class, String.class);
        metodo.setAccessible(true);
        assertThatThrownBy(() -> metodo.invoke(service, null, "e-mail direto"))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Template ausente");
    }

    @Test
    @DisplayName("getEmailUnidade deve gerar endereco com sigla minuscula")
    void getEmailUnidadeDeveGerarEnderecoComSiglaMinuscula() {
        assertThat(service.getEmailUnidade(criarUnidade(1L, "ABC", "Unidade"))).isEqualTo("abc@tre-pe.jus.br");
    }

    @Test
    @DisplayName("notificarAlteracaoDataLimite deve criar alerta e enfileirar email no outbox")
    void notificarAlteracaoDataLimiteDeveCriarAlertaEEnfileirarEmailNoOutbox() {
        Unidade unidade = criarUnidade(10L, "ORIG", "Unidade origem");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setCodigo(99L);

        when(alertaService.criarAlertaAlteracaoDataLimite(processo, unidade, "25/04/2026", 1))
                .thenReturn(alerta);
        when(templateEngine.process(eq("data-limite-alterada"), any(IContext.class)))
                .thenReturn("<html>data-limite</html>");

        service.notificarAlteracaoDataLimite(subprocesso, "25/04/2026", 1);

        verify(alertaService).criarAlertaAlteracaoDataLimite(processo, unidade, "25/04/2026", 1);
        verify(notificacaoEmailService).enfileirar(notificacaoEmailCaptor.capture());

        EnfileirarNotificacaoEmailCommand cmd = notificacaoEmailCaptor.getValue();
        assertThat(cmd.destinatario()).isEqualTo("orig@tre-pe.jus.br");
        assertThat(cmd.assunto()).isEqualTo("SGC: Data limite alterada");
        assertThat(cmd.corpoHtml()).isEqualTo("<html>data-limite</html>");
        assertThat(cmd.tipoNotificacao()).isEqualTo("DATA_LIMITE_ALTERADA");
        assertThat(cmd.alerta()).isSameAs(alerta);
        assertThat(cmd.subprocesso()).isSameAs(subprocesso);
        assertThat(cmd.chaveIdempotencia()).contains("data-limite-alterada").contains("etapa:1").contains("25/04/2026");
    }

    @Test
    @DisplayName("notificarAlteracaoDataLimite deve usar etapa 2 para subprocessos de mapa")
    void notificarAlteracaoDataLimiteDeveUsarEtapa2() {
        Unidade unidade = criarUnidade(10L, "ORIG", "Unidade origem");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();

        when(alertaService.criarAlertaAlteracaoDataLimite(processo, unidade, "30/04/2026", 2))
                .thenReturn(alerta);
        when(templateEngine.process(eq("data-limite-alterada"), any(IContext.class)))
                .thenReturn("<html>corpo</html>");

        service.notificarAlteracaoDataLimite(subprocesso, "30/04/2026", 2);

        verify(notificacaoEmailService).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().chaveIdempotencia()).contains("etapa:2");
    }

    private Processo criarProcesso() {
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
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
