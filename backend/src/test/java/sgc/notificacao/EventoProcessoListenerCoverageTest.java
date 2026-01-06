package sgc.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerCoverageTest {

    @InjectMocks
    private EventoProcessoListener listener;

    @Mock private AlertaService servicoAlertas;
    @Mock private NotificacaoEmailService notificacaoEmailService;
    @Mock private NotificacaoModelosService notificacaoModelosService;
    @Mock private UsuarioService usuarioService;
    @Mock private ProcessoService processoService;
    @Mock private SubprocessoService subprocessoService;

    // --- Testes para aoIniciarProcesso ---

    private EventoProcessoIniciado criarEvento(Long codProcesso, TipoProcesso tipo) {
        return EventoProcessoIniciado.builder()
                .codProcesso(codProcesso)
                .tipo(tipo.name())
                .dataHoraInicio(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("aoIniciarProcesso: deve capturar exceção geral")
    void aoIniciarProcesso_CapturaException() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        when(processoService.buscarEntidadePorId(1L)).thenThrow(new RuntimeException("Erro"));

        assertThatCode(() -> listener.aoIniciarProcesso(evento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("aoIniciarProcesso: retorna se subprocessos vazio")
    void aoIniciarProcesso_SemSubprocessos() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L);
        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(Collections.emptyList());

        listener.aoIniciarProcesso(evento);

        verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: subprocesso sem unidade deve ser ignorado")
    void aoIniciarProcesso_SubprocessoSemUnidade() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(null); // Sem unidade

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        // Mocks removidos pois não são chamados quando unidade é null

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: unidade sem responsavel deve ser ignorada")
    void aoIniciarProcesso_UnidadeSemResponsavel() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L);
        Unidade u = new Unidade(); u.setCodigo(10L);
        Subprocesso sp = new Subprocesso(); sp.setUnidade(u);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(servicoAlertas.criarAlertasProcessoIniciado(any(), any())).thenReturn(Collections.emptyList());
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Collections.emptyMap()); // Vazio

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: responsavel sem titular deve ser ignorado")
    void aoIniciarProcesso_ResponsavelSemTitular() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L);
        Unidade u = new Unidade(); u.setCodigo(10L);
        Subprocesso sp = new Subprocesso(); sp.setUnidade(u);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

        ResponsavelDto resp = new ResponsavelDto();
        resp.setTitularTitulo(null); // Null
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: titular sem email deve ser ignorado")
    void aoIniciarProcesso_TitularSemEmail() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L); p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade(); u.setCodigo(10L);
        Subprocesso sp = new Subprocesso(); sp.setUnidade(u);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

        ResponsavelDto resp = new ResponsavelDto();
        resp.setTitularTitulo("123");
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));

        UsuarioDto user = new UsuarioDto();
        user.setEmail(""); // Empty
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", user));

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: tipo unidade desconhecido deve ser ignorado")
    void aoIniciarProcesso_TipoDesconhecido() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L); p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade(); u.setCodigo(10L); u.setTipo(TipoUnidade.RAIZ); // Não tratado no IF (RAIZ)
        Subprocesso sp = new Subprocesso(); sp.setUnidade(u);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

        ResponsavelDto resp = new ResponsavelDto(); resp.setTitularTitulo("123");
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));
        UsuarioDto user = new UsuarioDto(); user.setEmail("a@a.com");
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", user));

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: erro no envio de email deve ser capturado")
    void aoIniciarProcesso_ErroEnvioEmail() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L); p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade(); u.setCodigo(10L); u.setTipo(TipoUnidade.OPERACIONAL);
        Subprocesso sp = new Subprocesso(); sp.setUnidade(u);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

        ResponsavelDto resp = new ResponsavelDto(); resp.setTitularTitulo("123");
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));
        UsuarioDto user = new UsuarioDto(); user.setEmail("a@a.com");
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", user));

        doThrow(new RuntimeException("SMTP Error")).when(notificacaoEmailService).enviarEmailHtml(any(), any(), any());

        assertThatCode(() -> listener.aoIniciarProcesso(evento)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("aoIniciarProcesso: deve enviar para substituto")
    void aoIniciarProcesso_EnviaParaSubstituto() {
        EventoProcessoIniciado evento = criarEvento(1L, TipoProcesso.MAPEAMENTO);
        Processo p = new Processo(); p.setCodigo(1L); p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade(); u.setCodigo(10L); u.setTipo(TipoUnidade.OPERACIONAL);
        Subprocesso sp = new Subprocesso(); sp.setUnidade(u);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

        ResponsavelDto resp = new ResponsavelDto();
        resp.setTitularTitulo("123");
        resp.setSubstitutoTitulo("456"); // Substituto
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));

        UsuarioDto titular = new UsuarioDto(); titular.setEmail("t@t.com");
        UsuarioDto subst = new UsuarioDto(); subst.setEmail("s@s.com");
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", titular, "456", subst));

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, times(2)).enviarEmailHtml(any(), any(), any());
    }

    // --- Testes para aoFinalizarProcesso ---

    @Test
    @DisplayName("aoFinalizarProcesso: deve capturar exceção geral")
    void aoFinalizarProcesso_CapturaException() {
        EventoProcessoFinalizado evento = new EventoProcessoFinalizado(1L, LocalDateTime.now());
        when(processoService.buscarEntidadePorId(1L)).thenThrow(new RuntimeException("Erro"));

        assertThatCode(() -> listener.aoFinalizarProcesso(evento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("aoFinalizarProcesso: retorna se participantes vazio")
    void aoFinalizarProcesso_SemParticipantes() {
        EventoProcessoFinalizado evento = new EventoProcessoFinalizado(1L, LocalDateTime.now());
        Processo p = new Processo(); p.setCodigo(1L); p.setParticipantes(Set.of());
        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);

        listener.aoFinalizarProcesso(evento);

        verify(usuarioService, never()).buscarResponsaveisUnidades(any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso: erro unidade individual deve ser capturado e continuar")
    void aoFinalizarProcesso_ErroIndividual() {
        EventoProcessoFinalizado evento = new EventoProcessoFinalizado(1L, LocalDateTime.now());
        Unidade u1 = new Unidade(); u1.setCodigo(1L);
        Unidade u2 = new Unidade(); u2.setCodigo(2L);
        Processo p = new Processo(); p.setCodigo(1L); p.setParticipantes(Set.of(u1, u2));

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        // Retorna apenas responsavel de U2, causando erro em U1
        ResponsavelDto r2 = new ResponsavelDto(); r2.setTitularTitulo("999");
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(2L, r2));

        UsuarioDto user2 = new UsuarioDto(); user2.setEmail("u2@u.com");
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("999", user2));

        assertThatCode(() -> listener.aoFinalizarProcesso(evento)).doesNotThrowAnyException();
        // Deve tentar enviar pelo menos um, mas o tipo de unidade é null então não enviará de fato
        // mas o importante é não quebrar.
    }

    @Test
    @DisplayName("aoFinalizarProcesso: Unidade Intermediaria sem subordinadas participantes nao envia email")
    void aoFinalizarProcesso_IntermediariaSemSubordinadas() {
        EventoProcessoFinalizado evento = new EventoProcessoFinalizado(1L, LocalDateTime.now());
        Unidade inter = new Unidade(); inter.setCodigo(1L); inter.setSigla("INTER"); inter.setTipo(TipoUnidade.INTERMEDIARIA);
        // Intermediária participa sozinha, sem subordinadas no processo
        Processo p = new Processo(); p.setCodigo(1L); p.setParticipantes(Set.of(inter));

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        ResponsavelDto r = new ResponsavelDto(); r.setTitularTitulo("123");
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(1L, r));
        UsuarioDto user = new UsuarioDto(); user.setEmail("a@a.com");
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", user));

        listener.aoFinalizarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: deve capturar exceção durante envio de e-mail")
    void aoIniciarProcesso_ExceptionNoEnvio() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = criarEvento(codProcesso, TipoProcesso.MAPEAMENTO);

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade Teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());

        ResponsavelDto responsavel = new ResponsavelDto();
        responsavel.setTitularTitulo("user1");
        responsavel.setSubstitutoTitulo("user2");

        UsuarioDto titular = new UsuarioDto();
        titular.setEmail("titular@test.com");

        UsuarioDto substituto = new UsuarioDto();
        substituto.setEmail("substituto@test.com");

        when(processoService.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, responsavel));
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("user1", titular, "user2", substituto));
        when(notificacaoModelosService.criarEmailDeProcessoIniciado(anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Erro de teste"));

        assertThatCode(() -> listener.aoIniciarProcesso(evento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviarEmailDeProcessoIniciado: deve logar aviso se subprocesso sem unidade")
    void enviarEmail_SubprocessoSemUnidade() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = criarEvento(codProcesso, TipoProcesso.MAPEAMENTO);

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(null);

        when(processoService.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        // Não é necessário mockar buscarResponsaveisUnidades e buscarUsuariosPorTitulos 
        // porque o método retorna early quando unidade é null

        assertThatCode(() -> listener.aoIniciarProcesso(evento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviarEmailDeProcessoIniciado: deve processar unidade INTERMEDIARIA")
    void enviarEmail_UnidadeIntermediaria() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = criarEvento(codProcesso, TipoProcesso.MAPEAMENTO);

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Intermediaria");
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());

        ResponsavelDto responsavel = new ResponsavelDto();
        responsavel.setTitularTitulo("user1");

        UsuarioDto titular = new UsuarioDto();
        titular.setEmail("titular@test.com");

        when(processoService.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, responsavel));
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("user1", titular));
        when(notificacaoModelosService.criarEmailDeProcessoIniciado(anyString(), anyString(), anyString(), any()))
                .thenReturn("<html>Email</html>");

        assertThatCode(() -> listener.aoIniciarProcesso(evento))
                .doesNotThrowAnyException();

        verify(notificacaoEmailService, atLeastOnce()).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("enviarEmailParaSubstituto: deve capturar exceção")
    void enviarEmailParaSubstituto_Exception() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = criarEvento(codProcesso, TipoProcesso.MAPEAMENTO);

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade Teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());

        ResponsavelDto responsavel = new ResponsavelDto();
        responsavel.setTitularTitulo("user1");
        responsavel.setSubstitutoTitulo("user2");

        UsuarioDto titular = new UsuarioDto();
        titular.setEmail("titular@test.com");

        UsuarioDto substituto = new UsuarioDto();
        substituto.setEmail("substituto@test.com");

        when(processoService.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, responsavel));
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("user1", titular, "user2", substituto));
        when(notificacaoModelosService.criarEmailDeProcessoIniciado(anyString(), anyString(), anyString(), any()))
                .thenReturn("<html>Email</html>");
        doThrow(new RuntimeException("Erro no envio")).when(notificacaoEmailService).enviarEmailHtml(eq("substituto@test.com"), anyString(), anyString());

        assertThatCode(() -> listener.aoIniciarProcesso(evento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enviarEmailDeProcessoIniciado: deve processar unidade INTEROPERACIONAL")
    void enviarEmail_UnidadeInteroperacional() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = criarEvento(codProcesso, TipoProcesso.MAPEAMENTO);

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Interoperacional");
        unidade.setTipo(TipoUnidade.INTEROPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());

        ResponsavelDto responsavel = new ResponsavelDto();
        responsavel.setTitularTitulo("user1");

        UsuarioDto titular = new UsuarioDto();
        titular.setEmail("titular@test.com");

        when(processoService.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, responsavel));
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("user1", titular));
        when(notificacaoModelosService.criarEmailDeProcessoIniciado(anyString(), anyString(), anyString(), any()))
                .thenReturn("<html>Email</html>");

        assertThatCode(() -> listener.aoIniciarProcesso(evento))
                .doesNotThrowAnyException();

        verify(notificacaoEmailService, atLeastOnce()).enviarEmailHtml(anyString(), anyString(), anyString());
    }
}
