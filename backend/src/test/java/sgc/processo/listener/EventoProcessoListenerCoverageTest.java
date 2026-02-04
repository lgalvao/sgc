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

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra: EventoProcessoListener")
class EventoProcessoListenerCoverageTest {

    @Mock private AlertaFacade servicoAlertas;
    @Mock private NotificacaoEmailService notificacaoEmailService;
    @Mock private NotificacaoModelosService notificacaoModelosService;
    @Mock private UnidadeFacade unidadeService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private ProcessoFacade processoFacade;
    @Mock private SubprocessoFacade subprocessoFacade;

    @InjectMocks
    private EventoProcessoListener listener;

    @Test
    @DisplayName("IniciarProcesso: Unidade RAIZ deve logar ErroEstadoImpossivel (sem explodir)")
    void aoIniciarProcesso_UnidadeRaiz_CapturaErro() {
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(1L)
                .tipo("MAPEAMENTO")
                .dataHoraInicio(java.time.LocalDateTime.now())
                .codUnidades(List.of(100L))
                .build();
                
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setCodigo(100L);
        u.setTipo(TipoUnidade.RAIZ);
        sp.setUnidade(u);
        
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                100L, new UnidadeResponsavelDto(100L, "Chefe", "NomeC", "Sub", "NomeS")
        ));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "Chefe", new UsuarioDto("Chefe", "NomeC", "email@test.com", "123", 100L),
                "Sub", new UsuarioDto("Sub", "NomeS", "sub@test.com", "124", 100L)
        ));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> listener.aoIniciarProcesso(evento));
        
        verify(notificacaoModelosService, never()).criarEmailProcessoIniciado(any(), any(), any(), any());
    }

    @Test
    @DisplayName("IniciarProcesso: Responsável não encontrado deve retornar silenciosamente")
    void aoIniciarProcesso_ResponsavelNaoEncontrado() {
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(1L)
                .build();
                
        Processo processo = new Processo();
        
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setCodigo(100L);
        sp.setUnidade(u);
        
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of()); // Mapa vazio

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> listener.aoIniciarProcesso(evento));
        
        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("IniciarProcesso: Erro ao enviar email substituto deve ser capturado")
    void aoIniciarProcesso_ErroEmailSubstituto_CapturaExcecao() {
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(1L)
                .build();
                
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setCodigo(100L);
        u.setTipo(TipoUnidade.OPERACIONAL);
        sp.setUnidade(u);
        
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                100L, new UnidadeResponsavelDto(100L, "Chefe", "NomeC", "Sub", "NomeS")
        ));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "Chefe", new UsuarioDto("Chefe", "NomeC", "email@test.com", "123", 100L),
                "Sub", new UsuarioDto("Sub", "NomeS", "sub@test.com", "124", 100L)
        ));
        
        // Simular erro no envio para substituto
        doThrow(new RuntimeException("Falha SMTP")).when(notificacaoEmailService)
            .enviarEmailHtml(eq("sub@test.com"), anyString(), any());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> listener.aoIniciarProcesso(evento));
    }

    @Test
    @DisplayName("FinalizarProcesso: Unidade Intermediária deve filtrar subordinadas corretamente")
    void aoFinalizarProcesso_Intermediaria_FiltroSubordinadas() {
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder()
                .codProcesso(1L)
                .build();
                
        Processo processo = new Processo();
        
        Unidade intermediaria = new Unidade();
        intermediaria.setCodigo(10L);
        intermediaria.setTipo(TipoUnidade.INTERMEDIARIA);
        intermediaria.setSigla("INT");
        
        Unidade sub1 = new Unidade();
        sub1.setCodigo(20L);
        sub1.setUnidadeSuperior(intermediaria); // Match
        
        Unidade sub2 = new Unidade();
        sub2.setCodigo(30L);
        sub2.setUnidadeSuperior(null); // No match
        
        Unidade sub3 = new Unidade();
        sub3.setCodigo(40L);
        Unidade outra = new Unidade();
        outra.setCodigo(99L);
        sub3.setUnidadeSuperior(outra); // No match
        
        processo.setParticipantes(java.util.Set.of(intermediaria, sub1, sub2, sub3));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                10L, new UnidadeResponsavelDto(10L, "Chefe", "NomeC", null, null)
        ));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "Chefe", new UsuarioDto("Chefe", "NomeC", "email@test.com", "123", 10L)
        ));

        listener.aoFinalizarProcesso(evento);
        
        // Só deve listar sub1
        verify(notificacaoModelosService).criarEmailProcessoFinalizadoUnidadesSubordinadas(
                eq("INT"), any(), argThat(list -> list.size() == 1));
    }
}
