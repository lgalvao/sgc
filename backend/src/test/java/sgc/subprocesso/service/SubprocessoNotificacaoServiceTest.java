package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoNotificacaoService")
@SuppressWarnings("NullAway.Init")
class SubprocessoNotificacaoServiceTest {

    @Mock
    private AlertaFacade alertaService;
    @Mock
    private NotificacaoService notificacaoService;
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
    private ArgumentCaptor<EnfileirarNotificacaoCommand> notificacaoEmailCaptor;

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

        when(responsavelService.buscarResponsavelUnidadeOpt(destino.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .substitutoTitulo("substituto")
                        .substitutoNome("Substituto")
                        .build()));
        when(usuarioService.buscarOpt("substituto"))
                .thenReturn(Optional.of(criarUsuario("substituto@tre-pe.jus.br")));

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .observacoes("Observacao importante")
                .build());

        verify(alertaService).criarAlertaTransicao(
                eq(processo),
                eq(TipoTransicao.CADASTRO_DISPONIBILIZADO.formatarAlerta("ORIG")),
                eq(origem),
                eq(destino));
        verify(notificacaoService, times(2)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoCommand::destinatario)
                .containsExactly("dest@tre-pe.jus.br", "substituto@tre-pe.jus.br");
        assertThat(notificacaoEmailCaptor.getAllValues())
                .allSatisfy(cmd -> {
                    assertThat(cmd.assunto()).startsWith("SGC: ");
                    assertThat(cmd.corpoHtml()).isEqualTo("<html>corpo</html>");
                    assertThat(cmd.tipoNotificacao()).isEqualTo(TipoNotificacao.CADASTRO_DISPONIBILIZADO);
                    assertThat(cmd.chaveIdempotencia()).contains("transicao:CADASTRO_DISPONIBILIZADO");
                });
        verify(templateEngine).process(templateCaptor.capture(), contextCaptor.capture());

        assertThat(templateCaptor.getValue()).isEqualTo("cadastro-disponibilizado");
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

        when(responsavelService.buscarResponsavelUnidadeOpt(destino.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(20L);
        when(unidadeService.buscarResumosPorCodigos(List.of(20L))).thenReturn(List.of(
                new UnidadeResumoLeitura(20L, "Superior imediato", "SUP1", TipoUnidade.INTERMEDIARIA)
        ));

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(notificacaoService, times(2)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoCommand::destinatario)
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

        when(responsavelService.buscarResponsavelUnidadeOpt(destino.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(20L);

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(notificacaoService, times(1)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().destinatario()).isEqualTo("dest@tre-pe.jus.br");
        verify(unidadeService, never()).buscarResumosPorCodigos(anyList());
    }

    @Test
    @DisplayName("deve notificar superior direto da unidade do subprocesso na reabertura")
    void deveNotificarSuperiorDiretoDaUnidadeDoSubprocessoNaReabertura() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade admin = criarUnidade(1L, "ADMIN", "Administracao");
        Unidade unidade = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(10L, "ORIG", "Unidade origem");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);

        when(responsavelService.buscarResponsavelUnidadeOpt(destino.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(20L);
        when(unidadeService.buscarResumosPorCodigos(List.of(20L))).thenReturn(List.of(
                new UnidadeResumoLeitura(20L, "Superior direto", "SUP", TipoUnidade.INTERMEDIARIA)
        ));

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_REABERTO)
                .unidadeOrigem(admin)
                .unidadeDestino(destino)
                .observacoes("Ajustar cadastro")
                .build());

        verify(alertaService, never()).criarAlertaTransicao(any(), anyString(), any(), any());
        verify(notificacaoService, times(2)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoCommand::destinatario)
                .containsExactly("orig@tre-pe.jus.br", "sup@tre-pe.jus.br");
    }

    @Test
    @DisplayName("nao deve enviar email pessoal quando substituto nao possuir email")
    void naoDeveEnviarEmailPessoalQuandoSubstitutoNaoPossuirEmail() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        when(responsavelService.buscarResponsavelUnidadeOpt(destino.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .substitutoTitulo("substituto")
                        .substitutoNome("Substituto")
                        .build()));
        when(usuarioService.buscarOpt("substituto"))
                .thenReturn(Optional.of(criarUsuario("")));

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(notificacaoService, times(1)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().destinatario()).isEqualTo("dest@tre-pe.jus.br");
    }

    @Test
    @DisplayName("deve falhar transacao e nao enviar email quando criacao de alerta falhar")
    void deveFalharTransacaoENaoEnviarEmailQuandoCriacaoDeAlertaFalhar() {
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        doThrow(new IllegalStateException("falha alerta"))
                .when(alertaService).criarAlertaTransicao(any(), anyString(), any(), any());

        assertThatThrownBy(() -> service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falha alerta");

        verify(notificacaoService, never()).enfileirar(any(EnfileirarNotificacaoCommand.class));
    }

    @Test
    @DisplayName("nao deve acionar alerta ou email quando a transicao nao exigir notificacao")
    void naoDeveAcionarAlertaOuEmailQuandoATransicaoNaoExigirNotificacao() {
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_HOMOLOGADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verifyNoInteractions(alertaService, notificacaoService, responsavelService, usuarioService, templateEngine);
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
        verify(notificacaoService).enfileirar(notificacaoEmailCaptor.capture());

        EnfileirarNotificacaoCommand cmd = notificacaoEmailCaptor.getValue();
        assertThat(cmd.destinatario()).isEqualTo("orig@tre-pe.jus.br");
        assertThat(cmd.assunto()).isEqualTo("SGC: Data limite alterada");
        assertThat(cmd.corpoHtml()).isEqualTo("<html>data-limite</html>");
        assertThat(cmd.tipoNotificacao()).isEqualTo(TipoNotificacao.DATA_LIMITE_ALTERADA);
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

        verify(notificacaoService).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().chaveIdempotencia()).contains("etapa:2");
    }

    @Test
    @DisplayName("notificarAlteracaoDataLimite deve falhar transacao e nao enfileirar email quando criacao de alerta falhar")
    void notificarAlteracaoDataLimiteDeveFalharTransacaoENaoEnfileirarEmailQuandoAlertaFalhar() {
        Unidade unidade = criarUnidade(10L, "ORIG", "Unidade origem");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);

        doThrow(new IllegalStateException("falha alerta"))
                .when(alertaService).criarAlertaAlteracaoDataLimite(processo, unidade, "25/04/2026", 1);

        assertThatThrownBy(() -> service.notificarAlteracaoDataLimite(subprocesso, "25/04/2026", 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falha alerta");

        verify(notificacaoService, never()).enfileirar(any(EnfileirarNotificacaoCommand.class));
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

    @Test
    @DisplayName("nao deve enfileirar email para superior quando superior for nulo")
    void naoDeveEnfileirarEmailParaSuperiorQuandoSuperiorForNulo() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade superiorImediato = criarUnidade(20L, "SUP1", "Superior imediato");
        Unidade destino = criarUnidade(30L, "DEST", "Unidade destino");
        destino.setUnidadeSuperior(superiorImediato);

        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        origem.setUnidadeSuperior(superiorImediato);

        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        when(responsavelService.buscarResponsavelUnidadeOpt(destino.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(20L);
        when(unidadeService.buscarResumosPorCodigos(List.of(20L))).thenReturn(List.of());

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verify(notificacaoService, times(1)).enfileirar(any());
    }

    @Test
    @DisplayName("registrarComunicacoesTransicao deve ignorar envio de e-mails para transições sem template configurado")
    void registrarComunicacoesTransicao_DeveIgnorarEnvioDeEmailsQuandoNaoHouverTemplate() {
        Unidade origem = criarUnidade(10L, "ORIG", "Origem");
        Unidade destino = criarUnidade(20L, "DEST", "Destino");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.MAPA_HOMOLOGADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build());

        verifyNoInteractions(alertaService, notificacaoService);
    }

    @Test
    @DisplayName("obterTemplateObrigatorio deve lancar IllegalStateException quando template for nulo ou em branco")
    void obterTemplateObrigatorio_DeveLancarErroQuandoNuloOuEmBranco() {
        // Caso 1: Nulo
        assertThatThrownBy(() -> service.obterTemplateObrigatorio(null, "contexto nulo"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template ausente para contexto nulo");

        // Caso 2: Em branco
        assertThatThrownBy(() -> service.obterTemplateObrigatorio("   ", "contexto vazio"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template ausente para contexto vazio");
    }

    @Test
    @DisplayName("não deve gerar notificações em bloco quando a lista estiver vazia")
    void naoDeveGerarNotificacoesEmBlocoQuandoAListaEstiverVazia() {
        service.notificarAceiteCadastroEmBloco(List.of());
        service.notificarDisponibilizacaoMapaEmBloco(List.of());
        service.notificarAceiteValidacaoEmBloco(List.of());

        verifyNoInteractions(notificacaoService, templateEngine, unidadeService, usuarioService, responsavelService);
    }

    @Test
    @DisplayName("deve criar assunto de revisão de cadastro reaberta com a sigla da unidade")
    void deveCriarAssuntoDeRevisaoDeCadastroReabertaComASiglaDaUnidade() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade admin = criarUnidade(1L, "ADMIN", "Administracao");
        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);

        when(responsavelService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(unidade.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));
        when(unidadeHierarquiaService.buscarCodigoPai(unidade.getCodigo())).thenReturn(null);

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_REABERTA)
                .unidadeOrigem(admin)
                .unidadeDestino(unidade)
                .build());

        verify(notificacaoService).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().assunto()).isEqualTo("SGC: Reabertura de revisão de cadastro - ORIG");
    }

    @Test
    @DisplayName("notificarHomologacaoMapa deve usar o assunto padrão do tipo de transição")
    void notificarHomologacaoMapaDeveUsarOAssuntoPadraoDoTipoDeTransicao() {
        when(templateEngine.process(eq("mapa-homologado"), any(IContext.class))).thenReturn("<html>homologado</html>");

        Unidade admin = criarUnidade(1L, "ADMIN", "Administracao");
        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);

        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(responsavelService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(unidade.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));

        service.notificarHomologacaoMapa(subprocesso);

        verify(notificacaoService).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getValue().assunto()).isEqualTo("SGC: Mapa de competências homologado");
    }

    @Test
    @DisplayName("criarAssunto deve retornar texto específico para cadastro homologado")
    void criarAssuntoDeveRetornarTextoEspecificoParaCadastroHomologado() {
        String assunto = service.criarAssunto(
                TipoTransicao.CADASTRO_HOMOLOGADO,
                criarSubprocesso(criarUnidade(10L, "ORIG", "Origem"), criarProcesso()),
                false
        );

        assertThat(assunto).isEqualTo("SGC: Cadastro de atividades homologado");
    }

    @Test
    @DisplayName("não deve notificar superior quando o comando desabilitar explicitamente essa notificação")
    void naoDeveNotificarSuperiorQuandoOComandoDesabilitarExplicitamenteEssaNotificacao() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade origem = criarUnidade(10L, "ORIG", "Origem");
        Unidade destino = criarUnidade(20L, "DEST", "Destino");
        destino.setUnidadeSuperior(criarUnidade(30L, "SUP", "Superior"));
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        when(responsavelService.buscarResponsavelUnidadeOpt(destino.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(destino.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));

        service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .notificarSuperior(Boolean.FALSE)
                .build());

        verify(notificacaoService, times(1)).enfileirar(any());
        verify(unidadeService, never()).buscarResumosPorCodigos(anyList());
    }

    @Test
    @DisplayName("notificarAceiteCadastroEmBloco deve ignorar subprocessos sem superior imediato")
    void notificarAceiteCadastroEmBlocoDeveIgnorarSubprocessosSemSuperiorImediato() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        unidade.setUnidadeSuperior(null);
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);

        when(unidadeService.buscarAdmin()).thenReturn(criarUnidade(1L, "ADMIN", "Administracao"));
        when(responsavelService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(unidade.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));

        service.notificarAceiteCadastroEmBloco(List.of(subprocesso));

        verify(notificacaoService, times(1)).enfileirar(any());
    }

    @Test
    @DisplayName("criarAssunto deve usar fallback para tipos não mapeados explicitamente")
    void criarAssuntoDeveUsarFallbackParaTiposNaoMapeadosExplicitamente() {
        String assunto = service.criarAssunto(
                TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA,
                criarSubprocesso(criarUnidade(10L, "ORIG", "Origem"), criarProcesso()),
                false
        );

        assertThat(assunto).isEqualTo("SGC: " + TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA.getDescMovimentacao());
    }

    @Test
    @DisplayName("notificarAceiteCadastroEmBloco deve usar templates e assuntos de revisão")
    void notificarAceiteCadastroEmBlocoDeveUsarTemplatesEAssuntosDeRevisao() {
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>corpo</html>");

        Unidade superior = criarUnidade(20L, "SUP", "Superior");
        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        unidade.setUnidadeSuperior(superior);
        Processo processo = criarProcesso();
        processo.setTipo(TipoProcesso.REVISAO);
        Subprocesso subprocesso = criarSubprocesso(unidade, processo);

        when(unidadeService.buscarAdmin()).thenReturn(criarUnidade(1L, "ADMIN", "Administracao"));
        when(responsavelService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.of(UnidadeResponsavelDto.builder()
                        .unidadeCodigo(unidade.getCodigo())
                        .titularTitulo("titular")
                        .titularNome("Titular")
                        .build()));

        service.notificarAceiteCadastroEmBloco(List.of(subprocesso));

        verify(templateEngine, times(2)).process(templateCaptor.capture(), any(IContext.class));
        assertThat(templateCaptor.getAllValues())
                .containsExactly("revisao-cadastro-aceita-bloco-unidade", "revisao-cadastro-aceita-bloco-superior");

        verify(notificacaoService, times(2)).enfileirar(notificacaoEmailCaptor.capture());
        assertThat(notificacaoEmailCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoCommand::tipoNotificacao)
                .containsOnly(TipoNotificacao.REVISAO_CADASTRO_ACEITA);
        assertThat(notificacaoEmailCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoCommand::assunto)
                .containsExactly(
                        "SGC: Revisão do cadastro de atividades e conhecimentos da ORIG submetido para análise",
                        "SGC: Revisões de cadastro de atividades e conhecimentos submetidas para análise"
                );
    }



    @Test
    @DisplayName("deve tolerar falha no envio de email e manter criacao de alerta")
    void deveTolerarFalhaNoEnvioDeEmailEManterCriacaoDeAlerta() {
        Unidade origem = criarUnidade(10L, "ORIG", "Unidade origem");
        Unidade destino = criarUnidade(20L, "DEST", "Unidade destino");
        Processo processo = criarProcesso();
        Subprocesso subprocesso = criarSubprocesso(origem, processo);

        doThrow(new RuntimeException("Erro infraestrutura de e-mail"))
                .when(responsavelService).buscarResponsavelUnidadeOpt(destino.getCodigo());

        assertThatCode(() -> service.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoTransicao(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build())).doesNotThrowAnyException();

        verify(alertaService).criarAlertaTransicao(
                eq(processo),
                eq(TipoTransicao.CADASTRO_DISPONIBILIZADO.formatarAlerta("ORIG")),
                eq(origem),
                eq(destino));
    }


    @Test
    @DisplayName("deve lancar IllegalStateException quando obter template obrigatorio com valor nulo ou em branco")
    void deveLancarIllegalStateExceptionQuandoObterTemplateObrigatorioComValorInvalido() {
        assertThatThrownBy(() -> service.obterTemplateObrigatorio(null, "contexto teste"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template ausente para contexto teste");

        assertThatThrownBy(() -> service.obterTemplateObrigatorio("   ", "contexto teste"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template ausente para contexto teste");
    }



}

