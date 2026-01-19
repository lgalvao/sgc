package sgc.processo.listener;

import org.junit.jupiter.api.DisplayName;
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
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerCoverageTest {

    @Mock private AlertaFacade servicoAlertas;
    @Mock private NotificacaoEmailService notificacaoEmailService;
    @Mock private NotificacaoModelosService notificacaoModelosService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private ProcessoFacade processoFacade;
    @Mock private SubprocessoFacade subprocessoFacade;

    @InjectMocks
    private EventoProcessoListener listener;

    @Test
    @DisplayName("aoIniciarProcesso - Tipo Unidade Desconhecido (Default Case)")
    void aoIniciarProcesso_TipoUnidadeDesconhecido() {
        // Covers lines 127-128 (default case in switch throwing ErroEstadoImpossivel)
        // And exception catching block

        Long codProcesso = 1L;
        Long codUnidade = 100L;

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("Processo Teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        unidade.setTipo(TipoUnidade.SEM_EQUIPE); // This should trigger default case
        unidade.setNome("Unidade Sem Equipe");
        unidade.setSigla("USE");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));

        ResponsavelDto resp = ResponsavelDto.builder()
                .titularTitulo("123")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(codUnidade, resp));

        UsuarioDto usuario = UsuarioDto.builder()
                .email("test@test.com")
                .build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", usuario));

        listener.aoIniciarProcesso(evento);

        // Verify that email service was NOT called because of exception
        verify(notificacaoEmailService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("aoIniciarProcesso - Com Substituto")
    void aoIniciarProcesso_ComSubstituto() {
        // Covers lines 261-262 (sending email to substitute)

        Long codProcesso = 1L;
        Long codUnidade = 100L;

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("Processo Teste");
        processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO); // valid type

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setNome("Unidade Operacional");
        unidade.setSigla("UOP");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(java.time.LocalDateTime.now());

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));

        ResponsavelDto resp = ResponsavelDto.builder()
                .titularTitulo("123")
                .substitutoTitulo("456") // Has substitute
                .build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(codUnidade, resp));

        UsuarioDto titular = UsuarioDto.builder()
                .email("titular@test.com")
                .build();
        UsuarioDto substituto = UsuarioDto.builder()
                .email("substituto@test.com")
                .build();

        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", titular, "456", substituto));
        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("<html></html>");

        listener.aoIniciarProcesso(evento);

        // Verify email sent to substitute
        verify(notificacaoEmailService).enviarEmailHtml(eq("substituto@test.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Exception Handling")
    void aoFinalizarProcesso_ExceptionHandling() {
        // Covers lines 180-182 (catch block in enviarNotificacaoFinalizacao)

        Long codProcesso = 1L;
        Long codUnidade = 100L;

        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        // We will make getTipo() throw exception by using a spy? Or better, make one of the service calls throw exception inside the method.
        // enviarNotificacaoFinalizacao calls:
        // 1. responsaveis.get()
        // 2. usuarios.get()
        // 3. unidade.getTipo()
        // 4. enviarEmailUnidadeFinal or enviarEmailUnidadeIntermediaria

        // Let's make ResponsavelDto retrieval ok, but User retrieval fail or something inside calls fail.
        // Actually, the loop iterates units.
        processo.setParticipantes(java.util.Set.of(unidade));

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);

        ResponsavelDto resp = ResponsavelDto.builder()
                .titularTitulo("123")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(codUnidade, resp));

        // make usuarios return null or empty map so titular lookup returns null?
        // if titular is null, it returns early (line 170). Need to throw Exception.
        // Let's mock Unidade to throw exception on getTipo()? No, it's a POJO.
        // Let's mock notificacaoModelosService to throw exception.

        UsuarioDto titular = UsuarioDto.builder()
                .email("titular@test.com")
                .build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", titular));

        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSigla("SIGLA");

        when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenThrow(new RuntimeException("Error generation"));

        listener.aoFinalizarProcesso(evento);

        // Verify logging happened (we can't easily verify log, but we can verify execution flow didn't crash)
        verify(notificacaoEmailService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Top Level Exception")
    void aoFinalizarProcesso_TopLevelException() {
        // Covers lines 86-87 (top level catch block)

        Long codProcesso = 1L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder()
                .codProcesso(codProcesso)
                .build();

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenThrow(new RuntimeException("DB Error"));

        listener.aoFinalizarProcesso(evento);

        // Should not throw, but log error
        verify(processoFacade).buscarEntidadePorId(codProcesso);
    }

    @Test
    @DisplayName("aoIniciarProcesso - Sem Subprocessos")
    void aoIniciarProcesso_SemSubprocessos() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(codProcesso).build();

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(new Processo());
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(java.util.Collections.emptyList());

        listener.aoIniciarProcesso(evento);

        verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso - Erro Envio Email")
    void aoIniciarProcesso_ErroEnvioEmail() {
        Long codProcesso = 1L;
        Long codUnidade = 100L;
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(codProcesso).build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("P1");
        processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(10L);
        sp.setUnidade(unidade);
        sp.setProcesso(processo);

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));

        ResponsavelDto resp = ResponsavelDto.builder().titularTitulo("T1").build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(codUnidade, resp));

        UsuarioDto user = UsuarioDto.builder().email("email@test.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("T1", user));

        // Mock email service to throw exception
        doThrow(new RuntimeException("Email failed")).when(notificacaoEmailService).enviarEmailHtml(any(), any(), any());
        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("html");

        listener.aoIniciarProcesso(evento);

        // Should not crash, but log error
        verify(notificacaoEmailService).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Sem Participantes")
    void aoFinalizarProcesso_SemParticipantes() {
        Long codProcesso = 1L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder().codProcesso(codProcesso).build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setParticipantes(java.util.Collections.emptySet());

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);

        listener.aoFinalizarProcesso(evento);

        verify(usuarioService, never()).buscarResponsaveisUnidades(any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Sem Responsavel")
    void aoFinalizarProcesso_SemResponsavel() {
        Long codProcesso = 1L;
        Long codUnidade = 100L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder().codProcesso(codProcesso).build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        processo.setParticipantes(Set.of(unidade));

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of()); // No responsavel for this unit

        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of());

        listener.aoFinalizarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Sem Titular")
    void aoFinalizarProcesso_SemTitular() {
        Long codProcesso = 1L;
        Long codUnidade = 100L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder().codProcesso(codProcesso).build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        processo.setParticipantes(Set.of(unidade));

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);

        ResponsavelDto resp = ResponsavelDto.builder().titularTitulo("T1").build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(codUnidade, resp));

        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of()); // Titular not found

        listener.aoFinalizarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Unidade Intermediaria")
    void aoFinalizarProcesso_UnidadeIntermediaria() {
        Long codProcesso = 1L;
        Long codUnidade = 100L;
        Long codSubordinada = 101L;

        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder().codProcesso(codProcesso).build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("P1");

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);
        unidade.setSigla("U_INT");

        Unidade subordinada = new Unidade();
        subordinada.setCodigo(codSubordinada);
        subordinada.setUnidadeSuperior(unidade);
        subordinada.setSigla("U_SUB");

        processo.setParticipantes(Set.of(unidade, subordinada));

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);

        ResponsavelDto resp = ResponsavelDto.builder().titularTitulo("T1").build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(codUnidade, resp, codSubordinada, ResponsavelDto.builder().titularTitulo("T2").build()));

        UsuarioDto user = UsuarioDto.builder().email("email@test.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("T1", user, "T2", UsuarioDto.builder().email("e2").build()));

        when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any())).thenReturn("html");

        listener.aoFinalizarProcesso(evento);

        verify(notificacaoEmailService, atLeastOnce()).enviarEmailHtml(eq("email@test.com"), any(), any());
        verify(notificacaoModelosService).criarEmailProcessoFinalizadoUnidadesSubordinadas(eq("U_INT"), eq("P1"), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso - Exception In Loop")
    void aoIniciarProcesso_ExceptionInLoop() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(codProcesso).build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("P1");
        processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);

        Subprocesso sp1 = new Subprocesso();
        sp1.setCodigo(10L);
        sp1.setUnidade(new Unidade());
        sp1.getUnidade().setCodigo(100L);
        sp1.getUnidade().setSigla("U1");
        sp1.getUnidade().setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso sp2 = new Subprocesso();
        sp2.setCodigo(20L);
        sp2.setUnidade(new Unidade());
        sp2.getUnidade().setCodigo(200L);
        sp2.getUnidade().setSigla("U2");
        sp2.getUnidade().setTipo(TipoUnidade.OPERACIONAL);

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp1, sp2));

        // Return empty map causes NPE in enviarEmailProcessoIniciado because responsavel is null
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of());
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of());

        listener.aoIniciarProcesso(evento);

        // Verify loop ran for both (implied by no crash)
        verify(subprocessoFacade).listarEntidadesPorProcesso(codProcesso);
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Exception In Notification")
    void aoFinalizarProcesso_ExceptionInNotification() {
        Long codProcesso = 1L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder().codProcesso(codProcesso).build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        Unidade u1 = new Unidade();
        u1.setCodigo(100L);
        processo.setParticipantes(Set.of(u1));

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);

        // Throw exception when getting responsaveis
        when(usuarioService.buscarResponsaveisUnidades(any())).thenThrow(new RuntimeException("Error fetching responsaveis"));

        listener.aoFinalizarProcesso(evento);

        // Should catch and log
        verify(processoFacade).buscarEntidadePorId(codProcesso);
    }
}
