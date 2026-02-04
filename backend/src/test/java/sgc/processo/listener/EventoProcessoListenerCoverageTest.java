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
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerCoverageTest {

    @InjectMocks
    private EventoProcessoListener listener;

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

    @Test
    @DisplayName("aoIniciarProcesso com unidade INTERMEDIARIA envia email apenas para titular se substituto ausente")
    void aoIniciarProcesso_Intermediaria() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);
        unidade.setNome("Unidade Inter");
        unidade.setSigla("UI");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);

        // SubstitutoTitulo defined but no user found in map later
        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("TITULAR")
                .substitutoTitulo("SUBSTITUTO") 
                .build();
        
        UsuarioDto usuarioTitular = UsuarioDto.builder()
                .tituloEleitoral("TITULAR")
                .nome("nome")
                .email("email@test.com")
                .build();

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, resp));
        // Mocking user service to ONLY return titular
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("TITULAR", usuarioTitular));
        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("Conteudo Email");

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService).enviarEmailHtml(eq("email@test.com"), contains("Processo Iniciado em Unidades Subordinadas"), anyString());
    }

    @Test
    @DisplayName("aoIniciarProcesso quando responsavel da unidade nao encontrado deve retornar early")
    void aoIniciarProcesso_ResponsavelNaoEncontrado() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setDescricao("Processo");
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        Unidade unidade = new Unidade();
        unidade.setCodigo(99L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSigla("U99");
        subprocesso.setUnidade(unidade);

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        // Return empty map for responsaveis
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of());
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of());

        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }
    void aoIniciarProcesso_SubstitutoSuccess() {
        Long codProcesso = 1L;
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setNome("Unidade Op");
        unidade.setSigla("UO");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);

        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("TITULAR")
                .substitutoTitulo("SUBSTITUTO")
                .build();
        
        UsuarioDto usuarioTitular = UsuarioDto.builder()
                .tituloEleitoral("TITULAR")
                .nome("nome")
                .email("email@test.com")
                .build();
                
        UsuarioDto usuarioSubstituto = UsuarioDto.builder()
                .tituloEleitoral("SUBSTITUTO")
                .nome("sub")
                .email("sub@test.com")
                .build();

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, resp));
        when(usuarioService.buscarUsuariosPorTitulos(anyList()))
            .thenReturn(Map.of("TITULAR", usuarioTitular, "SUBSTITUTO", usuarioSubstituto));
        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("Conteudo Email");

        listener.aoIniciarProcesso(evento);

        // Verify that emails were sent to both
        verify(notificacaoEmailService).enviarEmailHtml(eq("email@test.com"), anyString(), anyString());
        verify(notificacaoEmailService).enviarEmailHtml(eq("sub@test.com"), anyString(), anyString());
    }
}
