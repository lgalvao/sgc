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
    @Test
    @DisplayName("aoIniciarProcesso deve capturar exceção ao enviar e-mail para substituto")
    void aoIniciarProcesso_SubstitutoError() {
        Long cod = 100L;
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(cod).build();

        Processo p = new Processo();
        p.setDescricao("Desc");
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setTipo(TipoUnidade.OPERACIONAL);
        u.setSigla("UO");

        Subprocesso s = new Subprocesso();
        s.setUnidade(u);

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("T")
                .substitutoTitulo("S")
                .build();
        
        UsuarioDto ut = UsuarioDto.builder().tituloEleitoral("T").email("t@t.com").build();
        UsuarioDto us = UsuarioDto.builder().tituloEleitoral("S").email("s@s.com").build();

        when(processoFacade.buscarEntidadePorId(cod)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(cod)).thenReturn(List.of(s));
        when(unidadeService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, r));
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("T", ut, "S", us));
        when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("html");
        
        doThrow(new RuntimeException("SIMULADO")).when(notificacaoEmailService)
            .enviarEmailHtml(eq("s@s.com"), anyString(), anyString());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> listener.aoIniciarProcesso(evento));

        verify(notificacaoEmailService, times(2)).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("aoFinalizarProcesso deve enviar e-mails para unidades operacionais e intermediárias")
    void aoFinalizarProcesso_SucessoTotal() {
        Long codProcesso = 1L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("Processo Finalizado");

        Unidade uInter = new Unidade();
        uInter.setCodigo(1L);
        uInter.setTipo(TipoUnidade.INTERMEDIARIA);
        uInter.setSigla("UI");

        Unidade uOp = new Unidade();
        uOp.setCodigo(2L);
        uOp.setTipo(TipoUnidade.OPERACIONAL);
        uOp.setSigla("UO");
        uOp.setUnidadeSuperior(uInter);

        Unidade uOutra = new Unidade();
        uOutra.setCodigo(3L);
        uOutra.setTipo(TipoUnidade.OPERACIONAL);
        uOutra.setSigla("UX");
        uOutra.setUnidadeSuperior(null);

        processo.setParticipantes(java.util.Set.of(uInter, uOp, uOutra));

        UnidadeResponsavelDto rInter = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L).titularTitulo("T1").build();
        UnidadeResponsavelDto rOp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(2L).titularTitulo("T2").build();
        UnidadeResponsavelDto rOutra = UnidadeResponsavelDto.builder()
                .unidadeCodigo(3L).titularTitulo("T3").build();

        UsuarioDto t1 = UsuarioDto.builder().tituloEleitoral("T1").email("t1@test.com").build();
        UsuarioDto t2 = UsuarioDto.builder().tituloEleitoral("T2").email("t2@test.com").build();
        UsuarioDto t3 = UsuarioDto.builder().tituloEleitoral("T3").email("t3@test.com").build();

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, rInter, 2L, rOp, 3L, rOutra));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", t1, "T2", t2, "T3", t3));
        
        when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("html op");
        when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), anyList())).thenReturn("html inter");

        listener.aoFinalizarProcesso(evento);

        verify(notificacaoEmailService, times(1)).enviarEmailHtml(eq("t1@test.com"), anyString(), eq("html inter"));
        verify(notificacaoEmailService, times(1)).enviarEmailHtml(eq("t2@test.com"), anyString(), eq("html op"));
    }

    @Test
    @DisplayName("aoFinalizarProcesso deve tratar unidades sem superiores ou superiores diferentes")
    void aoFinalizarProcesso_FiltragemSubordinadas() {
        Long codProcesso = 1L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder()
                .codProcesso(codProcesso)
                .build();

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("Processo");

        Unidade uInter = new Unidade();
        uInter.setCodigo(1L);
        uInter.setTipo(TipoUnidade.INTERMEDIARIA);
        uInter.setSigla("UI");

        Unidade uSuperiorDiferente = new Unidade();
        uSuperiorDiferente.setCodigo(9L);

        Unidade uSubordinadaInvalida = new Unidade();
        uSubordinadaInvalida.setCodigo(2L);
        uSubordinadaInvalida.setUnidadeSuperior(uSuperiorDiferente);

        processo.setParticipantes(java.util.Set.of(uInter, uSubordinadaInvalida));

        UnidadeResponsavelDto rInter = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L).titularTitulo("T1").build();
        UsuarioDto t1 = UsuarioDto.builder().tituloEleitoral("T1").email("t1@test.com").build();

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenReturn(processo);
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, rInter));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", t1));

        listener.aoFinalizarProcesso(evento);

        // UI não deve enviar e-mail pois não tem subordinadas na lista de participantes (da mesma UI)
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq("t1@test.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("aoFinalizarProcesso deve capturar exceção genérica no loop de envio")
    void aoFinalizarProcesso_ErroNoLoop() {
        Long codProcesso = 1L;
        sgc.processo.eventos.EventoProcessoFinalizado evento = sgc.processo.eventos.EventoProcessoFinalizado.builder()
                .codProcesso(codProcesso)
                .build();

        when(processoFacade.buscarEntidadePorId(codProcesso)).thenThrow(new RuntimeException("Erro inesperado"));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> listener.aoFinalizarProcesso(evento));
    }
}
