package sgc.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.organizacao.UsuarioService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerCoverageTest {

    @InjectMocks
    private EventoProcessoListener listener;

    @Mock private AlertaFacade servicoAlertas;
    @Mock private NotificacaoEmailService notificacaoEmailService;
    @Mock private NotificacaoModelosService notificacaoModelosService;
    @Mock private UsuarioService usuarioService;
    @Mock private ProcessoFacade processoFacade;
    @Mock private SubprocessoFacade subprocessoFacade;

    // --- AO INICIAR PROCESSO ---

    @Test
    @DisplayName("aoIniciarProcesso: deve capturar exceção geral e logar")
    void aoIniciarProcesso_Excecao() {
        when(processoFacade.buscarEntidadePorId(any())).thenThrow(new RuntimeException("Erro"));
        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());
        // Se não lançar exceção, passou
    }

    @Test
    @DisplayName("aoIniciarProcesso: early return se lista de subprocessos vazia")
    void aoIniciarProcesso_SemSubprocessos() {
        Processo p = new Processo();
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(Collections.emptyList());

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());
        verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
    }

    @Test
    @DisplayName("aoIniciarProcesso: deve enviar emails (operacional e intermediaria)")
    void aoIniciarProcesso_Sucesso() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade uOp = new Unidade();
        uOp.setTipo(TipoUnidade.OPERACIONAL);
        uOp.setCodigo(1L);

        Subprocesso sOp = new Subprocesso();
        sOp.setUnidade(uOp);

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sOp));

        ResponsavelDto resp = ResponsavelDto.builder()
            .unidadeCodigo(1L)
            .titularTitulo("titular")
            .substitutoTitulo("substituto")
            .build();

        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(1L, resp));

        UsuarioDto userTitular = UsuarioDto.builder().tituloEleitoral("titular").email("t@email.com").build();
        UsuarioDto userSubstituto = UsuarioDto.builder().tituloEleitoral("substituto").email("s@email.com").build();

        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of(
            "titular", userTitular,
            "substituto", userSubstituto
        ));

        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("HTML");

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());

        verify(notificacaoEmailService, times(2)).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("aoIniciarProcesso: captura erro individual ao enviar email")
    void aoIniciarProcesso_ErroNoLoop() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u = new Unidade(); u.setTipo(TipoUnidade.OPERACIONAL); u.setCodigo(1L);
        Subprocesso s = new Subprocesso(); s.setUnidade(u);

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        // Simular erro ao buscar responsáveis
        when(usuarioService.buscarResponsaveisUnidades(any())).thenThrow(new RuntimeException("Erro DB"));

        listener.aoIniciarProcesso(EventoProcessoIniciado.builder().codProcesso(1L).build());
        // Deve capturar e logar
    }

    // --- AO FINALIZAR PROCESSO ---

    @Test
    @DisplayName("aoFinalizarProcesso: deve capturar exceção geral")
    void aoFinalizarProcesso_Excecao() {
        when(processoFacade.buscarEntidadePorId(any())).thenThrow(new RuntimeException("Erro"));
        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());
    }

    @Test
    @DisplayName("aoFinalizarProcesso: early return se sem participantes")
    void aoFinalizarProcesso_SemParticipantes() {
        Processo p = new Processo(); // participantes vazio
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());
        verify(usuarioService, never()).buscarResponsaveisUnidades(any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso: envia email para Operacional")
    void aoFinalizarProcesso_Operacional() {
        Unidade u = new Unidade(); u.setCodigo(1L); u.setTipo(TipoUnidade.OPERACIONAL);
        Processo p = new Processo();
        p.setParticipantes(Set.of(u));
        p.setDescricao("Desc");

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);

        ResponsavelDto resp = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("t").build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(1L, resp));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("t").email("email").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("t", titular));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());

        verify(notificacaoModelosService).criarEmailProcessoFinalizadoPorUnidade(any(), any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso: envia email para Intermediaria com subordinadas")
    void aoFinalizarProcesso_Intermediaria() {
        Unidade pai = new Unidade(); pai.setCodigo(1L); pai.setTipo(TipoUnidade.INTERMEDIARIA); pai.setSigla("PAI");
        Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setUnidadeSuperior(pai); filho.setSigla("FILHO");

        Processo p = new Processo();
        p.setParticipantes(Set.of(pai, filho));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);

        ResponsavelDto respPai = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("t").build();
        // Filho não precisa de responsável para este teste, pois só a intermediária é notificada sobre subordinadas

        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(1L, respPai));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("t").email("email").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("t", titular));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());

        verify(notificacaoModelosService).criarEmailProcessoFinalizadoUnidadesSubordinadas(eq("PAI"), any(), argThat(list -> list.contains("FILHO")));
    }

    @Test
    @DisplayName("aoFinalizarProcesso: intermediaria sem subordinadas avisa no log e retorna")
    void aoFinalizarProcesso_IntermediariaSemFilhos() {
        Unidade pai = new Unidade(); pai.setCodigo(1L); pai.setTipo(TipoUnidade.INTERMEDIARIA);

        Processo p = new Processo();
        p.setParticipantes(Set.of(pai)); // Só o pai participa

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);

        ResponsavelDto respPai = ResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("t").build();
        when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(1L, respPai));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("t").email("email").build();
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("t", titular));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());

        verify(notificacaoModelosService, never()).criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any());
    }

    @Test
    @DisplayName("aoFinalizarProcesso: captura erro dentro do loop")
    void aoFinalizarProcesso_ErroLoop() {
        Unidade u = new Unidade(); u.setCodigo(1L);
        Processo p = new Processo(); p.setParticipantes(Set.of(u));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(usuarioService.buscarResponsaveisUnidades(any())).thenThrow(new RuntimeException("DB"));

        listener.aoFinalizarProcesso(EventoProcessoFinalizado.builder().codProcesso(1L).build());
        // não explode
    }

    // --- Switch Case Coverages ---

    @Test
    @DisplayName("criarCorpoEmailPorTipo: deve lançar erro para tipo desconhecido (SEM_EQUIPE)")
    void criarCorpoEmailPorTipo_Erro() {
        Subprocesso s = new Subprocesso();
        Unidade u = new Unidade();
        u.setTipo(TipoUnidade.SEM_EQUIPE); // Tipo não tratado no switch
        s.setUnidade(u);

        // Precisamos forçar a chamada. O método é package-private.
        Processo processo = new Processo();
        org.junit.jupiter.api.Assertions.assertThrows(
                ErroEstadoImpossivel.class,
                () -> listener.criarCorpoEmailPorTipo(TipoUnidade.SEM_EQUIPE, processo, s));
    }
}
