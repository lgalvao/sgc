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
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setNome("U1");

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
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setNome("U1");
        
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
        UsuarioDto substitutoSemEmail = UsuarioDto.builder().tituloEleitoral("2").email(null).build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("1", titular, "2", substitutoSemEmail));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, times(1)).enviarEmailHtml(anyString(), anyString(), anyString());
        verify(notificacaoEmailService).enviarEmailHtml(eq("t@t.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve logar erro para unidade RAIZ (não suportada)")
    void deveLogarErroParaTipoUnidadeRaiz() {
        configurarMockParaTipo(TipoUnidade.RAIZ);
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);
        verify(notificacaoEmailService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve logar erro para unidade SEM_EQUIPE (não suportada)")
    void deveLogarErroParaTipoUnidadeSemEquipe() {
        configurarMockParaTipo(TipoUnidade.SEM_EQUIPE);
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);
        verify(notificacaoEmailService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    private void configurarMockParaTipo(TipoUnidade tipo) {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(99L);
        unidade.setTipo(tipo);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder().unidadeCodigo(99L).titularTitulo("9").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(99L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("9").email("u@u.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("9", titular));
    }

    @Test
    @DisplayName("Deve ignorar se lista de subprocessos for nula")
    void deveIgnorarSeListaSubprocessosNula() {
        assertThatCode(() -> listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve ignorar se lista de subprocessos for vazia")
    void deveIgnorarSeListaSubprocessosVazia() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setParticipantes(Set.of());
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);

        assertThatCode(() -> listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve disparar e-mails ao finalizar processo")
    void deveDispararEmailsAoFinalizar() {
        Processo processo = new Processo();
        processo.setCodigo(2L);
        processo.setDescricao("P2");
        when(processoFacade.buscarEntidadePorId(2L)).thenReturn(processo);

        Unidade inter = new Unidade();
        inter.setCodigo(44L);
        inter.setSigla("INTER");
        inter.setTipo(TipoUnidade.INTERMEDIARIA);

        processo.setParticipantes(Set.of(inter));

        ResponsavelDto resp = ResponsavelDto.builder()
                .unidadeCodigo(44L)
                .titularTitulo("444")
                .substitutoTitulo("555")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(44L, resp));

        UsuarioDto userInter = UsuarioDto.builder().tituloEleitoral("444").email("inter@mail.com").build();
        
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("444", userInter));
        
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(2L).build();
        listener.aoFinalizarProcesso(evento);
        
        verify(notificacaoEmailService).enviarEmailHtml(eq("inter@mail.com"), anyString(), any());
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
