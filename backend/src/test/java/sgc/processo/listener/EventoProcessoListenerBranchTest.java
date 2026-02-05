package sgc.processo.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventoProcessoListener Branch Coverage Tests")
class EventoProcessoListenerBranchTest {

    @InjectMocks
    private EventoProcessoListener listener;

    @Mock private ProcessoFacade processoFacade;
    @Mock private UnidadeFacade unidadeService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private NotificacaoModelosService notificacaoModelosService;
    @Mock private NotificacaoEmailService notificacaoEmailService;

    @Test
    @DisplayName("Deve cobrir todas as branches do filtro de subordinadas (Linha 213)")
    void deveCobrirBranchesFiltroSubordinadas() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Proc");
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade intermediaria = Unidade.builder().codigo(10L).sigla("INT").tipo(TipoUnidade.INTERMEDIARIA).build();

        // Unidade 1: unidadeSuperior é null
        Unidade u1 = Unidade.builder().codigo(1L).sigla("U1").unidadeSuperior(null).build();

        // Unidade 2: unidadeSuperior não é a intermediária
        Unidade superiorOutra = Unidade.builder().codigo(99L).build();
        Unidade u2 = Unidade.builder().codigo(2L).sigla("U2").unidadeSuperior(superiorOutra).build();

        // Unidade 3: unidadeSuperior É a intermediária
        Unidade u3 = Unidade.builder().codigo(3L).sigla("U3").unidadeSuperior(intermediaria).build();

        processo.setParticipantes(Set.of(intermediaria, u1, u2, u3));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(processo);
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                10L, UnidadeResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("T10").build(),
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build(),
                3L, UnidadeResponsavelDto.builder().unidadeCodigo(3L).titularTitulo("T3").build()
        ));

        UsuarioDto user = UsuarioDto.builder().email("test@test.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T10", user, "T1", user, "T2", user, "T3", user
        ));

        lenient().when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("html");
        when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any())).thenReturn("html");

        // Act
        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());

        // Assert
        verify(notificacaoModelosService).criarEmailProcessoFinalizadoUnidadesSubordinadas(eq("INT"), any(), eq(List.of("U3")));
    }
}
