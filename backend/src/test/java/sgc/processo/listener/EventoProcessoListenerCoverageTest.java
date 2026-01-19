package sgc.processo.listener;

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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerCoverageTest {

    @InjectMocks
    private EventoProcessoListener listener;

    @Mock private AlertaFacade servicoAlertas;
    @Mock private NotificacaoEmailService notificacaoEmailService;
    @Mock private NotificacaoModelosService notificacaoModelosService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private ProcessoFacade processoFacade;
    @Mock private SubprocessoFacade subprocessoFacade;

    @Test
    void deveProcessarInicioComResponsavelSemTitular() {
        // Cobre branch 117 (titular null)
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);

        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        // Responsável SEM titular
        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo(null)
                .substitutoTitulo("Substituto")
                .build();

        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        // Deve buscar apenas pelo substituto
        UsuarioDto usuarioSubstituto = UsuarioDto.builder().tituloEleitoral("Substituto").email("sub@email.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("Substituto", usuarioSubstituto));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        // Verifica que tentou buscar (mas vai falhar no envio pois tenta pegar titular, mas isso gera exceção que é logada)
        // O importante é cobrir o loop de titulos
        verify(usuarioService).buscarUsuariosPorTitulos(argThat(list -> list.contains("Substituto") && !list.contains(null)));
    }

    @Test
    void deveProcessarInicioComIntermediariaESubordinadas() {
        // Cobre switch case INTERMEDIARIA (228 e 260)
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo P");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);
        unidade.setNome("Unidade Intermediaria");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());

        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("123")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("123").email("titular@email.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("123", titular));

        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any()))
                .thenReturn("Corpo Email");

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        // Verifica envio de email
        verify(notificacaoEmailService).enviarEmailHtml(
                eq("titular@email.com"),
                contains("Processo Iniciado em Unidades Subordinadas"), // Assunto INTERMEDIARIA
                eq("Corpo Email")
        );
    }

    @Test
    void deveIgnorarNotificacaoFinalizacaoSeTitularInvalido() {
        // Cobre branch 170: titular null ou email null
        Processo processo = new Processo();
        processo.setCodigo(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setTipo(TipoUnidade.OPERACIONAL);
        Unidade u2 = new Unidade(); u2.setCodigo(2L); u2.setTipo(TipoUnidade.OPERACIONAL);
        processo.setParticipantes(Set.of(u1, u2));

        ResponsavelDto r1 = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        ResponsavelDto r2 = ResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build();

        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1, 2L, r2));

        // T1 não existe no map de usuarios (null)
        // T2 existe mas email é null
        UsuarioDto user2 = UsuarioDto.builder().tituloEleitoral("T2").email(null).build();

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T2", user2));

        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();
        listener.aoFinalizarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    void deveEnviarEmailFinalizacaoInteroperacional() {
        // Cobre branch 175: INTEROPERACIONAL
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("P1");
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("U1");
        u1.setTipo(TipoUnidade.INTEROPERACIONAL);
        processo.setParticipantes(Set.of(u1));

        ResponsavelDto r1 = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1));

        UsuarioDto user1 = UsuarioDto.builder().tituloEleitoral("T1").email("t1@mail.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", user1));

        when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("HTML");

        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();
        listener.aoFinalizarProcesso(evento);

        verify(notificacaoEmailService).enviarEmailHtml(eq("t1@mail.com"), anyString(), eq("HTML"));
    }

    @Test
    void deveTratarErroEmAoIniciarProcesso() {
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        doThrow(new RuntimeException("Erro processando inicio")).when(processoFacade).buscarEntidadePorId(1L);

        listener.aoIniciarProcesso(evento);
        // Não deve lançar exceção
    }

    @Test
    void deveTratarErroEmAoFinalizarProcesso() {
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();
        doThrow(new RuntimeException("Erro processando fim")).when(processoFacade).buscarEntidadePorId(1L);

        listener.aoFinalizarProcesso(evento);
        // Não deve lançar exceção
    }

    @Test
    void deveTratarErroAoEnviarEmailProcessoIniciado() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u1 = new Unidade(); u1.setCodigo(10L);
        Subprocesso s1 = new Subprocesso(); s1.setCodigo(100L); s1.setUnidade(u1);

        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s1));
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of());

        // Responsavel null vai causar NPE ao tentar acessar no metodo enviarEmailProcessoIniciado ou antes
        // Na verdade, enviarEmailProcessoIniciado pega responsavel do map. Se null, tenta acessar getter -> NPE
        // Isso é capturado pelo catch dentro do loop

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);
    }

    @Test
    void deveTratarErroAoEnviarEmailSubstituto() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("P1");
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u1 = new Unidade(); u1.setCodigo(10L); u1.setTipo(TipoUnidade.OPERACIONAL); u1.setNome("U1");
        Subprocesso s1 = new Subprocesso(); s1.setCodigo(100L); s1.setUnidade(u1);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s1));

        ResponsavelDto r1 = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("T1")
                .substitutoTitulo("S1")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, r1));

        UsuarioDto t1 = UsuarioDto.builder().tituloEleitoral("T1").email("t1@mail.com").build();
        UsuarioDto s1Dto = UsuarioDto.builder().tituloEleitoral("S1").email("s1@mail.com").build();

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", t1, "S1", s1Dto));
        when(notificacaoModelosService.criarEmailProcessoIniciado(any(),any(),any(),any())).thenReturn("HTML");

        // Usando doAnswer para garantir que apenas o segundo email lance exceção
        doAnswer(invocation -> {
            String email = invocation.getArgument(0);
            if ("s1@mail.com".equals(email)) {
                throw new RuntimeException("Erro email substituto");
            }
            return null;
        }).when(notificacaoEmailService).enviarEmailHtml(any(), any(), any());

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService).enviarEmailHtml(eq("t1@mail.com"), any(), any());
        verify(notificacaoEmailService).enviarEmailHtml(eq("s1@mail.com"), any(), any());
    }

    @Test
    void deveTratarErroAoEnviarNotificacaoFinalizacao() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setTipo(TipoUnidade.OPERACIONAL); u1.setSigla("U1");
        processo.setParticipantes(Set.of(u1));

        ResponsavelDto r1 = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1));

        UsuarioDto t1 = UsuarioDto.builder().tituloEleitoral("T1").email("t1@mail.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", t1));

        // Forçar erro
        doThrow(new RuntimeException("Erro envio final"))
                .when(notificacaoModelosService).criarEmailProcessoFinalizadoPorUnidade(any(), any());

        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();
        listener.aoFinalizarProcesso(evento);
    }
}
