package sgc.processo.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
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
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private Processo criarProcesso(Long codigo) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo " + codigo);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(Collections.emptySet());
        return p;
    }

    @Test
    @DisplayName("Deve disparar e-mails ao iniciar processo")
    void deveDispararEmailsAoIniciar() {
        Processo processo = criarProcesso(1L);
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
        Unidade u = new Unidade(); u.setCodigo(1L); u.setTipo(TipoUnidade.RAIZ);
        s.setUnidade(u);
        
        // Testa ErroEstadoImpossivel ao criar corpo
        assertThatThrownBy(() -> listener.criarCorpoEmailPorTipo(TipoUnidade.RAIZ, processo, s))
                .isInstanceOf(sgc.comum.erros.ErroEstadoImpossivel.class);

        // Testa ErroEstadoImpossivel ao definir assunto no fluxo principal
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, ResponsavelDto.builder().titularTitulo("T").build()));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", UsuarioDto.builder().build()));
        
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());
        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir branches de substitutos")
    void deveCobrirBranchesSubstitutos() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u = new Unidade(); u.setCodigo(10L); u.setTipo(TipoUnidade.OPERACIONAL);
        Subprocesso s = new Subprocesso(); s.setUnidade(u);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        ResponsavelDto r = ResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("T").substitutoTitulo("S").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, r));

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

        Unidade operacional = new Unidade(); operacional.setCodigo(1L); operacional.setTipo(TipoUnidade.OPERACIONAL); operacional.setSigla("OP");
        Unidade intermediaria = new Unidade(); intermediaria.setCodigo(2L); intermediaria.setTipo(TipoUnidade.INTERMEDIARIA); intermediaria.setSigla("INT");
        
        processo.setParticipantes(Set.of(operacional, intermediaria));

        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            1L, ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
            2L, ResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build()
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
        Unidade sub = new Unidade(); sub.setCodigo(21L); sub.setTipo(TipoUnidade.OPERACIONAL); sub.setUnidadeSuperior(intermediaria); sub.setSigla("SUB");
        processo.setParticipantes(Set.of(operacional, intermediaria, sub));
        
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            1L, ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
            2L, ResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build(),
            21L, ResponsavelDto.builder().unidadeCodigo(21L).titularTitulo("TS").build()
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
        Unidade u = new Unidade(); u.setCodigo(1L); u.setTipo(TipoUnidade.SEM_EQUIPE);
        s.setUnidade(u);
        
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
        
        ResponsavelDto r = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", UsuarioDto.builder().email("t@mail.com").build()));

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir else implícito na finalização (tipo desconhecido)")
    void deveCobrirElseImplicitoFinalizacao() {
        Processo processo = criarProcesso(2L);
        when(processoFacade.buscarEntidadePorId(2L)).thenReturn(processo);

        Unidade raiz = new Unidade(); 
        raiz.setCodigo(1L); 
        raiz.setTipo(TipoUnidade.RAIZ); 
        
        processo.setParticipantes(Set.of(raiz));

        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            1L, ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build()
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
        
        verify(usuarioService, never()).buscarResponsaveisUnidades(anyList());
    }

    @Test
    @DisplayName("Deve capturar exceção no catch interno do loop de e-mails")
    void deveCapturarExcecaoNoCatchInterno() {
        Processo processo = criarProcesso(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u = new Unidade(); u.setCodigo(10L); u.setTipo(TipoUnidade.OPERACIONAL);
        Subprocesso s = new Subprocesso(); s.setCodigo(100L); s.setUnidade(u);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
        
        ResponsavelDto r = ResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("T").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, r));
        
        // Quando buscar usuário por titulo retornar null, o try interno vai estourar NPE
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Collections.emptyMap());

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }
}
