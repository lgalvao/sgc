package sgc.processo.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.alerta.AlertaFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private NotificacaoEmailService notificacaoEmailService;

    @Mock
    private NotificacaoModelosService notificacaoModelosService;

    @Mock
    private AlertaFacade servicoAlertas;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        lenient().when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        lenient().when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("corpo");
        lenient().when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any())).thenReturn("corpo");
    }

    @Test
    @DisplayName("Deve disparar e-mails ao iniciar processo")
    void deveDispararEmailsAoIniciar() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("P1");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("111")
                .substitutoTitulo("222")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("111").email("t@t.com").build();
        UsuarioDto substituto = UsuarioDto.builder().tituloEleitoral("222").email("s@s.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("111", titular, "222", substituto));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService).enviarEmailHtml(eq("t@t.com"), anyString(), any());
        verify(notificacaoEmailService).enviarEmailHtml(eq("s@s.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve enviar e-mail para substituto com sucesso")
    void deveEnviarEmailParaSubstitutoComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("1")
                .substitutoTitulo("2")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("1").email("t@t.com").build();
        UsuarioDto substituto = UsuarioDto.builder().tituloEleitoral("2").email("s@s.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("1", titular, "2", substituto));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService).enviarEmailHtml(eq("s@s.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve ignorar substituto se dados forem inválidos")
    void deveIgnorarSubstitutoSeDadosInvalidos() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("1")
                .substitutoTitulo("2")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("1").email("t@t.com").build();
        
        // Cobre email null
        UsuarioDto substitutoNullEmail = UsuarioDto.builder().tituloEleitoral("2").email(null).build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("1", titular, "2", substitutoNullEmail));
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        // Cobre email blank
        UsuarioDto substitutoBlankEmail = UsuarioDto.builder().tituloEleitoral("2").email("  ").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("1", titular, "2", substitutoBlankEmail));
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        // Cobre substituto null
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("1", titular));
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, times(3)).enviarEmailHtml(eq("t@t.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve capturar exceção no loop externo de inicialização")
    void deveCapturarExcecaoNoLoopExterno() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Subprocesso s = new Subprocesso();
        s.setUnidade(null); // Vai causar NPE antes do try interno
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        assertThatCode(() -> listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve cobrir branches no encerramento de processo")
    void deveCobrirBranchesFinalizacao() {
        Processo processo = new Processo();
        processo.setCodigo(2L);
        when(processoFacade.buscarEntidadePorId(2L)).thenReturn(processo);

        Unidade operacional = new Unidade();
        operacional.setCodigo(1L);
        operacional.setTipo(TipoUnidade.OPERACIONAL);

        Unidade intermediaria = new Unidade();
        intermediaria.setCodigo(2L);
        intermediaria.setTipo(TipoUnidade.INTERMEDIARIA);

        Unidade raiz = new Unidade();
        raiz.setCodigo(3L);
        raiz.setTipo(TipoUnidade.RAIZ); // Nao deve disparar email

        processo.setParticipantes(Set.of(operacional, intermediaria, raiz));

        ResponsavelDto respOp = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        ResponsavelDto respInt = ResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build();
        ResponsavelDto respRaiz = ResponsavelDto.builder().unidadeCodigo(3L).titularTitulo("T3").build();
        
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            1L, respOp, 2L, respInt, 3L, respRaiz
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
            "T1", UsuarioDto.builder().email("op@mail.com").build(),
            "T2", UsuarioDto.builder().email("int@mail.com").build(),
            "T3", UsuarioDto.builder().email("raiz@mail.com").build()
        ));

        // Para cobrir o log.warn em intermediaria sem subordinadas participantes
        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(2L).build());

        verify(notificacaoEmailService).enviarEmailHtml(eq("op@mail.com"), anyString(), any());
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq("int@mail.com"), anyString(), any());
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq("raiz@mail.com"), anyString(), any());

        // Agora com subordinadas para cobrir o sucesso em intermediaria
        Unidade sub = new Unidade();
        sub.setCodigo(21L);
        sub.setTipo(TipoUnidade.OPERACIONAL);
        sub.setUnidadeSuperior(intermediaria);
        processo.setParticipantes(Set.of(operacional, intermediaria, sub));

        ResponsavelDto respSub = ResponsavelDto.builder().unidadeCodigo(21L).titularTitulo("TS").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            1L, respOp, 2L, respInt, 21L, respSub
        ));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
            "T1", UsuarioDto.builder().email("op@mail.com").build(),
            "T2", UsuarioDto.builder().email("int@mail.com").build(),
            "TS", UsuarioDto.builder().email("sub@mail.com").build()
        ));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(2L).build());
        verify(notificacaoEmailService, times(1)).enviarEmailHtml(eq("int@mail.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve capturar exceção no envio de e-mail ao iniciar")
    void deveCapturarExcecaoNoEnvioAoIniciar() {
        lenient().when(processoFacade.buscarEntidadePorId(anyLong())).thenReturn(new Processo());
        lenient().when(subprocessoFacade.listarEntidadesPorProcesso(anyLong())).thenReturn(List.of(new Subprocesso()));
        lenient().doThrow(new RuntimeException("Erro de rede")).when(notificacaoEmailService).enviarEmailHtml(any(), any(), any());

        assertThatCode(() -> listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve ignorar processamento se não houver subprocessos ao iniciar")
    void deveIgnorarProcessamentoSeNaoHouverSubprocessosAoIniciar() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of());

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
    }
}
