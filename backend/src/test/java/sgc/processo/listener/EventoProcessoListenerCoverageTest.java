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
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("EventoProcessoListener - Cobertura Adicional")
class EventoProcessoListenerCoverageTest {

    @Mock
    private AlertaFacade servicoAlertas;
    @Mock
    private NotificacaoEmailService notificacaoEmailService;
    @Mock
    private NotificacaoModelosService notificacaoModelosService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private SubprocessoFacade subprocessoFacade;

    @InjectMocks
    private EventoProcessoListener listener;

    @Test
    @DisplayName("aoFinalizarProcesso deve tratar unidades INTEROPERACIONAL e RAIZ")
    void deveTratarUnidadesInteroperacionalERaiz() {
        // Arrange
        Long codProcesso = 1L;
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("Processo Teste");

        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        u1.setSigla("INTER");
        u1.setTipo(TipoUnidade.INTEROPERACIONAL);

        Unidade u2 = new Unidade();
        u2.setCodigo(1L);
        u2.setSigla("RAIZ");
        u2.setTipo(TipoUnidade.RAIZ);

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        // Simular que unidades participantes são u1 e u2
        // Na prática, processo.getCodigosParticipantes() precisaria retornar esses IDs
        // Mas o listener chama unidadeService.buscarEntidadesPorIds(codigos)
        
        // Mocking individual methods instead of dependency setup for simplicity in coverage
        when(unidadeService.buscarEntidadesPorIds(any())).thenReturn(List.of(u1, u2));
        
        UnidadeResponsavelDto resp1 = new UnidadeResponsavelDto(10L, "Tit1", "Nome1", "Sub1", "NomeSub1");
        UnidadeResponsavelDto resp2 = new UnidadeResponsavelDto(1L, "Tit2", "Nome2", null, null);
        when(unidadeService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp1, 1L, resp2));

        UsuarioDto user1 = UsuarioDto.builder().tituloEleitoral("Tit1").email("user1@test.com").build();
        UsuarioDto user2 = UsuarioDto.builder().tituloEleitoral("Tit2").email("user2@test.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("Tit1", user1, "Tit2", user2));

        when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(anyString(), anyString())).thenReturn("html");

        // Act
        listener.aoFinalizarProcesso(evento);

        // Assert
        verify(notificacaoEmailService, times(2)).enviarEmailHtml(anyString(), anyString(), anyString());
    }
}
