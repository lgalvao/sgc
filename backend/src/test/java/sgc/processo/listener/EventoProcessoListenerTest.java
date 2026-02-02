package sgc.processo.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;
import sgc.testutils.UnidadeTestBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("Testes do EventoProcessoListener")
class EventoProcessoListenerTest {

    @InjectMocks
    private EventoProcessoListener listener;

    @Mock
    private ProcessoFacade processoFacade;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private UnidadeFacade unidadeService;

    @Mock
    private NotificacaoEmailService notificacaoEmailService;

    @Mock
    private NotificacaoModelosService notificacaoModelosService;

    @Mock
    private AlertaFacade servicoAlertas;

    @BeforeEach
    void setup() {
        lenient().when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        lenient().when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("corpo");
        lenient().when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any())).thenReturn("corpo");
    }

    private Processo criarProcesso(Long codigo) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo " + codigo);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(Collections.emptySet());
        return p;
    }

    /**
     * Cria uma unidade com tipo e código customizado usando TestBuilder.
     * Elimina a necessidade de múltiplas linhas de setup.
     */
    private Unidade criarUnidade(Long codigo, TipoUnidade tipo) {
        return UnidadeTestBuilder.umaDe()
                .comCodigo(String.valueOf(codigo))
                .comTipo(tipo)
                .build();
    }

    @Test
    @DisplayName("Deve disparar e-mails ao iniciar processo")
    void deveDispararEmailsAoIniciar() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = criarUnidade(10L, TipoUnidade.OPERACIONAL);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        UnidadeResponsavelDto responsavel = UnidadeResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("111")
                .substitutoTitulo("222")
                .build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("111").email("t@t.com").build();
        UsuarioDto substituto = UsuarioDto.builder().tituloEleitoral("222").email("s@s.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("111", titular, "222", substituto));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService).enviarEmailHtml(eq("t@t.com"), anyString(), any());
        verify(notificacaoEmailService).enviarEmailHtml(eq("s@s.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve ignorar processamento se não houver subprocessos ao iniciar")
    void deveIgnorarProcessamentoSeNaoHouverSubprocessosAoIniciar() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of());

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
    }

    @Test
    @DisplayName("Deve cobrir exceções de tipo de unidade no início")
    void deveCobrirExcecaoTipoUnidadeRaizEInvalido() {
        Processo processo = criarProcesso(1L);
        Subprocesso s = new Subprocesso();
        Unidade u = criarUnidade(1L, TipoUnidade.RAIZ);
        s.setUnidade(u);

        // Testa ErroEstadoImpossivel ao criar corpo
        assertThatThrownBy(() -> listener.criarCorpoEmailPorTipo(TipoUnidade.RAIZ, processo, s))
                .isInstanceOf(ErroEstadoImpossivel.class);

        // Testa ErroEstadoImpossivel ao definir assunto no fluxo principal
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularTitulo("T").build()));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", UsuarioDto.builder().build()));

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());
        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir branches de substitutos")
    void deveCobrirBranchesSubstitutos() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u = criarUnidade(10L, TipoUnidade.OPERACIONAL);
        Subprocesso s = new Subprocesso();
        s.setUnidade(u);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("T").substitutoTitulo("S").build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, r));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("T").email("t@mail.com").build();

        // 1. Substituto null no mapa de usuários
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", titular));
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        // 2. Substituto com email null ou blank
        UsuarioDto subEmailNull = UsuarioDto.builder().tituloEleitoral("S").email(null).build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", titular, "S", subEmailNull));
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        UsuarioDto subEmailBlank = UsuarioDto.builder().tituloEleitoral("S").email("  ").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", titular, "S", subEmailBlank));
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, times(3)).enviarEmailHtml(eq("t@mail.com"), anyString(), anyString());
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq(null), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve cobrir branches na finalização de processo")
    void deveCobrirBranchesFinalizacao() {
        Processo processo = criarProcesso(2L);
        when(processoFacade.buscarEntidadePorId(2L)).thenReturn(processo);

        Unidade operacional = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        operacional.setSigla("OP");
        Unidade intermediaria = criarUnidade(2L, TipoUnidade.INTERMEDIARIA);
        intermediaria.setSigla("INT");

        processo.setParticipantes(Set.of(operacional, intermediaria));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", UsuarioDto.builder().email("op@mail.com").build(),
                "T2", UsuarioDto.builder().email("int@mail.com").build()
        ));

        // 1. Intermediária sem subordinadas (log.warn)
        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(2L).build());
        verify(notificacaoEmailService, times(1)).enviarEmailHtml(eq("op@mail.com"), anyString(), any());
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq("int@mail.com"), anyString(), any());

        // 2. Intermediária com subordinadas (sucesso)
        Unidade sub = criarUnidade(21L, TipoUnidade.OPERACIONAL);
        sub.setUnidadeSuperior(intermediaria);
        sub.setSigla("SUB");
        processo.setParticipantes(Set.of(operacional, intermediaria, sub));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build(),
                21L, UnidadeResponsavelDto.builder().unidadeCodigo(21L).titularTitulo("TS").build()
        ));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", UsuarioDto.builder().email("op@mail.com").build(),
                "T2", UsuarioDto.builder().email("int@mail.com").build(),
                "TS", UsuarioDto.builder().email("sub@mail.com").build()
        ));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(2L).build());
        verify(notificacaoEmailService).enviarEmailHtml(eq("int@mail.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir exceção para unidade SEM_EQUIPE no envio de email")
    void deveCobrirExcecaoSemEquipe() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Subprocesso s = new Subprocesso();
        Unidade u = criarUnidade(1L, TipoUnidade.SEM_EQUIPE);
        s.setUnidade(u);

        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T").build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", UsuarioDto.builder().email("t@mail.com").build()));

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir else implícito na finalização (tipo desconhecido)")
    void deveCobrirElseImplicitoFinalizacao() {
        Processo processo = criarProcesso(2L);
        when(processoFacade.buscarEntidadePorId(2L)).thenReturn(processo);

        Unidade raiz = criarUnidade(1L, TipoUnidade.RAIZ);

        processo.setParticipantes(Set.of(raiz));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", UsuarioDto.builder().email("op@mail.com").build()
        ));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(2L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve capturar exceção no processamento assíncrono geral")
    void deveCapturarExcecaoAsyncGeral() {
        when(processoFacade.buscarEntidadePorId(anyLong())).thenThrow(new RuntimeException("Crash"));

        assertThatCode(() -> listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build()))
                .doesNotThrowAnyException();
        assertThatCode(() -> listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve ignorar se participantes for vazio ao finalizar")
    void deveLogarWarningSeParticipantesVazio() {
        Processo p = criarProcesso(1L);
        p.setParticipantes(Collections.emptySet());
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());

        verify(unidadeService, never()).buscarResponsaveisUnidades(anyList());
    }

    @Test
    @DisplayName("Deve capturar exceção no catch interno do loop de e-mails")
    void deveCapturarExcecaoNoCatchInterno() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u = criarUnidade(10L, TipoUnidade.OPERACIONAL);
        Subprocesso s = new Subprocesso();
        s.setCodigo(100L);
        s.setUnidade(u);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("T").build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, r));

        // Quando buscar usuário por titulo retornar null, o try interno vai estourar NPE
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Collections.emptyMap());

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve ignorar participante sem responsável ou titular ao finalizar")
    void deveIgnorarParticipanteSemResponsavelOuTitularAoFinalizar() {
        Processo processo = criarProcesso(3L);
        when(processoFacade.buscarEntidadePorId(3L)).thenReturn(processo);

        Unidade u1 = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        Unidade u2 = criarUnidade(2L, TipoUnidade.OPERACIONAL);

        processo.setParticipantes(Set.of(u1, u2));

        // u1 sem responsável no mapa
        // u2 com responsável mas sem titularTitulo
        UnidadeResponsavelDto r2 = UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo(null).build();

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(2L, r2));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(3L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve ignorar participante com titular sem email ao finalizar")
    void deveIgnorarParticipanteComTitularSemEmailAoFinalizar() {
        Processo processo = criarProcesso(4L);
        when(processoFacade.buscarEntidadePorId(4L)).thenReturn(processo);

        Unidade u1 = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        Unidade u2 = criarUnidade(2L, TipoUnidade.OPERACIONAL);

        processo.setParticipantes(Set.of(u1, u2));

        UnidadeResponsavelDto r1 = UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        UnidadeResponsavelDto r2 = UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build();

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1, 2L, r2));

        // T1 com email null
        // T2 com email blank
        UsuarioDto user1 = UsuarioDto.builder().tituloEleitoral("T1").email(null).build();
        UsuarioDto user2 = UsuarioDto.builder().tituloEleitoral("T2").email("   ").build();

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", user1, "T2", user2));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(4L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção para SEM_EQUIPE")
    void deveLancarExcecaoParaSemEquipe() {
        Processo processo = criarProcesso(1L);
        Subprocesso s = new Subprocesso();
        Unidade u = criarUnidade(1L, TipoUnidade.SEM_EQUIPE);
        s.setUnidade(u);

        assertThatThrownBy(() -> listener.criarCorpoEmailPorTipo(TipoUnidade.SEM_EQUIPE, processo, s))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("Deve ignorar e-mail se titular inválido ao iniciar")
    void deveIgnorarEmailSeTitularInvalidoAoIniciar() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        // Unidade 1: Titular null no mapa
        Unidade u1 = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        Subprocesso s1 = new Subprocesso();
        s1.setUnidade(u1);

        // Unidade 2: Titular com email null
        Unidade u2 = criarUnidade(2L, TipoUnidade.OPERACIONAL);
        Subprocesso s2 = new Subprocesso();
        s2.setUnidade(u2);

        // Unidade 3: Titular com email blank
        Unidade u3 = criarUnidade(3L, TipoUnidade.OPERACIONAL);
        Subprocesso s3 = new Subprocesso();
        s3.setUnidade(u3);

        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s1, s2, s3));

        UnidadeResponsavelDto r1 = UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        UnidadeResponsavelDto r2 = UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build();
        UnidadeResponsavelDto r3 = UnidadeResponsavelDto.builder().unidadeCodigo(3L).titularTitulo("T3").build();

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1, 2L, r2, 3L, r3));

        UsuarioDto user2 = UsuarioDto.builder().tituloEleitoral("T2").email(null).build();
        UsuarioDto user3 = UsuarioDto.builder().tituloEleitoral("T3").email(" ").build();

        // T1 ausente no map
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T2", user2, "T3", user3));

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());

        // Verificar log warn (implícito via cobertura, se fosse logger mockado poderia verificar)
    }

    @Test
    @DisplayName("Deve cobrir tipo INTEROPERACIONAL na finalização")
    void deveCobrirTipoInteroperacionalNaFinalizacao() {
        Processo processo = criarProcesso(5L);
        when(processoFacade.buscarEntidadePorId(5L)).thenReturn(processo);

        Unidade interoperacional = criarUnidade(1L, TipoUnidade.INTEROPERACIONAL);
        interoperacional.setSigla("INTER");
        processo.setParticipantes(Set.of(interoperacional));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", UsuarioDto.builder().email("inter@mail.com").build()
        ));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(5L).build());

        verify(notificacaoEmailService).enviarEmailHtml(eq("inter@mail.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir exceção ao enviar email para unidade intermediária")
    void deveCobrirExcecaoAoEnviarEmailUnidadeIntermediaria() {
        Processo processo = criarProcesso(6L);
        when(processoFacade.buscarEntidadePorId(6L)).thenReturn(processo);

        Unidade intermediaria = criarUnidade(1L, TipoUnidade.INTERMEDIARIA);
        intermediaria.setSigla("INT");
        Unidade sub = criarUnidade(2L, TipoUnidade.OPERACIONAL);
        sub.setUnidadeSuperior(intermediaria);
        sub.setSigla("SUB");

        processo.setParticipantes(Set.of(intermediaria, sub));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", UsuarioDto.builder().email("int@mail.com").build(),
                "T2", UsuarioDto.builder().email("sub@mail.com").build()
        ));

        // Simular exceção ao criar email para unidade intermediária
        when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any()))
                .thenThrow(new RuntimeException("Erro ao criar template"));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(6L).build());

        // Deve ter tentado enviar para operacional (sucesso) mas não para intermediária (exceção)
        verify(notificacaoEmailService).enviarEmailHtml(eq("sub@mail.com"), anyString(), any());
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq("int@mail.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir substituto com email null")
    void deveCobrirSubstitutoComEmailNull() {
        Processo processo = criarProcesso(7L);
        when(processoFacade.buscarEntidadePorId(7L)).thenReturn(processo);

        Unidade u = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        u.setSigla("U1");
        u.setNome("Unidade 1");
        Subprocesso s = new Subprocesso();
        s.setUnidade(u);

        when(subprocessoFacade.listarEntidadesPorProcesso(7L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .titularTitulo("T1")
                .substitutoTitulo("S1")
                .build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("T1").email("t@mail.com").build();
        // Substituto com email null (NPE potencial)
        UsuarioDto substituto = UsuarioDto.builder().tituloEleitoral("S1").email(null).build();

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", titular, "S1", substituto));

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(7L).build());

        verify(notificacaoEmailService).enviarEmailHtml(eq("t@mail.com"), anyString(), any());
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq(null), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir responsáveis sem titulares na finalização")
    void deveCobrirResponsaveisSemTitularesNaFinalizacao() {
        Processo processo = criarProcesso(8L);
        when(processoFacade.buscarEntidadePorId(8L)).thenReturn(processo);

        Unidade u1 = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        Unidade u2 = criarUnidade(2L, TipoUnidade.OPERACIONAL);

        processo.setParticipantes(Set.of(u1, u2));

        // Responsáveis com titularTitulo null - stream resultará vazio
        UnidadeResponsavelDto r1 = UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo(null).build();
        UnidadeResponsavelDto r2 = UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo(null).build();

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1, 2L, r2));

        // Stream vazio leva a buscarUsuariosPorTitulos com lista vazia
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Collections.emptyMap());

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(8L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir catch de ErroEstadoImpossivel durante envio de email")
    void deveCobrirCatchErroEstadoImpossivelDuranteEnvioEmail() {
        Processo processo = criarProcesso(9L);
        when(processoFacade.buscarEntidadePorId(9L)).thenReturn(processo);

        Unidade u = criarUnidade(1L, TipoUnidade.RAIZ);
        u.setSigla("RAIZ");
        u.setNome("Unidade Raiz");
        Subprocesso s = new Subprocesso();
        s.setUnidade(u);

        when(subprocessoFacade.listarEntidadesPorProcesso(9L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .titularTitulo("T1")
                .build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("T1").email("t@mail.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", titular));

        // RAIZ lançará ErroEstadoImpossivel ao tentar criar assunto (linha 255)
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(9L).build());

        // Não deve enviar email pois exceção foi capturada
        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }
}
