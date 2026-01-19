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
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventoProcessoListenerCoverageTest")
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
    @DisplayName("aoIniciarProcesso - Sem Subprocessos")
    void aoIniciarProcesso_SemSubprocessos() {
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        Processo processo = new Processo();
        processo.setCodigo(1L);

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(Collections.emptyList());

        listener.aoIniciarProcesso(evento);

        verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso - Erro ao Enviar Email (Resiliencia)")
    void aoIniciarProcesso_ErroEnviarEmail() {
        // This covers lines 127-128 (catch block)
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("TITULAR")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto usuarioDto = UsuarioDto.builder().tituloEleitoral("TITULAR").email("email@teste.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", usuarioDto));

        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any()))
                .thenReturn("<html>Corpo</html>");

        // Simulate error sending email
        doThrow(new RuntimeException("Erro envio email"))
                .when(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());

        listener.aoIniciarProcesso(evento);

        // Verify that exception was caught and logged (execution continues)
        // Since we only have one subprocesso, we just verify the service was called
        verify(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Unidade Intermediaria com Subordinadas")
    void aoFinalizarProcesso_Intermediaria() {
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Finalizado");

        Unidade unidadeInter = new Unidade();
        unidadeInter.setCodigo(10L);
        unidadeInter.setSigla("INTER");
        unidadeInter.setTipo(TipoUnidade.INTERMEDIARIA);

        Unidade unidadeSub = new Unidade();
        unidadeSub.setCodigo(20L);
        unidadeSub.setSigla("SUB");
        unidadeSub.setUnidadeSuperior(unidadeInter); // Important for filter

        processo.setParticipantes(new HashSet<>(List.of(unidadeInter, unidadeSub)));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        ResponsavelDto respInter = ResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("TIT_INTER").build();
        ResponsavelDto respSub = ResponsavelDto.builder().unidadeCodigo(20L).titularTitulo("TIT_SUB").build();

        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, respInter, 20L, respSub));

        UsuarioDto usuarioInter = UsuarioDto.builder().tituloEleitoral("TIT_INTER").email("inter@teste.com").build();
        UsuarioDto usuarioSub = UsuarioDto.builder().tituloEleitoral("TIT_SUB").email("sub@teste.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TIT_INTER", usuarioInter, "TIT_SUB", usuarioSub));

        when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any()))
                .thenReturn("<html>Corpo Inter</html>");

        listener.aoFinalizarProcesso(evento);

        // Verify email sent to intermediary
        verify(notificacaoEmailService).enviarEmailHtml(eq("inter@teste.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Sem Unidades Participantes")
    void aoFinalizarProcesso_SemParticipantes() {
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setParticipantes(Collections.emptySet());

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        listener.aoFinalizarProcesso(evento);

        verify(usuarioService, never()).buscarResponsaveisUnidades(any());
    }

    @Test
    @DisplayName("enviarEmailParaSubstituto - Caminho Feliz")
    void enviarEmailParaSubstituto() {
        // Triggered via aoIniciarProcesso
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        subprocesso.setUnidade(unidade);

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("TITULAR")
                .substitutoTitulo("SUBSTITUTO")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto usuarioTitular = UsuarioDto.builder().tituloEleitoral("TITULAR").email("tit@teste.com").build();
        UsuarioDto usuarioSub = UsuarioDto.builder().tituloEleitoral("SUBSTITUTO").email("sub@teste.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(any()))
                .thenReturn(Map.of("TITULAR", usuarioTitular, "SUBSTITUTO", usuarioSub));

        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("<html></html>");

        listener.aoIniciarProcesso(evento);

        // Verify email sent to substitute
        verify(notificacaoEmailService).enviarEmailHtml(eq("sub@teste.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("aoIniciarProcesso - Subprocesso Sem Unidade (Catch Externo)")
    void aoIniciarProcesso_SubprocessoSemUnidade() {
        // Covers lines 127-128 (outer catch block) by triggering NPE before inner try
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        Processo processo = new Processo();
        processo.setCodigo(1L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(null); // Will cause NPE in enviarEmailProcessoIniciado

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        listener.aoIniciarProcesso(evento);

        // Verify log error (indirectly via no exception thrown)
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Erro Buscar Processo (Catch)")
    void aoFinalizarProcesso_ErroBuscarProcesso() {
        // Covers lines 86-87
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();

        when(processoFacade.buscarEntidadePorId(1L)).thenThrow(new RuntimeException("Erro busca"));

        listener.aoFinalizarProcesso(evento);

        // Execution continues
    }

    @Test
    @DisplayName("aoFinalizarProcesso - Erro Enviar Email (Catch Interno)")
    void aoFinalizarProcesso_ErroEnviarEmail() {
        // Covers lines 180-182
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(1L).build();
        Processo processo = new Processo();
        processo.setCodigo(1L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U1");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        processo.setParticipantes(new HashSet<>(List.of(unidade)));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);

        ResponsavelDto responsavel = ResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("TIT").build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto usuario = UsuarioDto.builder().tituloEleitoral("TIT").email("email@teste.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TIT", usuario));

        when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("html");

        doThrow(new RuntimeException("Erro email"))
                .when(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());

        listener.aoFinalizarProcesso(evento);

        // Verify exception was caught
        verify(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());
    }
}
