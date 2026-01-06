package sgc.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Listener de Eventos de Processo")
class EventoProcessoListenerTest {

    @Mock
    private AlertaService servicoAlertas;
    @Mock
    private NotificacaoEmailService notificacaoEmailService;
    @Mock
    private NotificacaoModelosService notificacaoModelosService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private ProcessoService processoService;
    @Mock
    private SubprocessoService subprocessoService;

    @InjectMocks
    private EventoProcessoListener listener;

    @Nested
    @DisplayName("Processo Iniciado")
    class InicioProcesso {

        @Test
        @DisplayName("Deve processar início de processo com sucesso")
        void deveProcessarInicioSucesso() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setDescricao("Processo 1");
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            Unidade u = new Unidade();
            u.setCodigo(10L);
            u.setNome("Unidade Teste");
            u.setSigla("U1");
            u.setTipo(TipoUnidade.OPERACIONAL);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));

            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
            when(servicoAlertas.criarAlertasProcessoIniciado(any(), any())).thenReturn(Collections.emptyList());

            ResponsavelDto resp = new ResponsavelDto();
            resp.setTitularTitulo("TITULAR");
            resp.setSubstitutoTitulo("SUBSTITUTO");
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));

            UsuarioDto titular = new UsuarioDto();
            titular.setEmail("titular@email.com");
            UsuarioDto substituto = new UsuarioDto();
            substituto.setEmail("sub@email.com");
            when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", titular, "SUBSTITUTO", substituto));

            when(notificacaoModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("html");

            listener.aoIniciarProcesso(new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of()));

            verify(notificacaoEmailService, times(2)).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve ignorar se não houver subprocessos")
        void deveIgnorarSemSubprocessos() {
            when(processoService.buscarEntidadePorId(1L)).thenReturn(new Processo());
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(Collections.emptyList());

            listener.aoIniciarProcesso(new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of()));

            verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
        }

        @Test
        @DisplayName("Deve capturar erro e não falhar")
        void deveCapturarErro() {
            when(processoService.buscarEntidadePorId(1L)).thenThrow(new RuntimeException("Erro banco"));

            assertThatCode(() -> listener.aoIniciarProcesso(new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of())))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com falta de responsável")
        void deveLidarFaltaResponsavel() {
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            Subprocesso sp = new Subprocesso();
            sp.setUnidade(u);

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Collections.emptyMap());

            listener.aoIniciarProcesso(new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of()));

            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve lidar com falta de email do titular")
        void deveLidarFaltaEmail() {
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            Subprocesso sp = new Subprocesso();
            sp.setUnidade(u);

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
            
            ResponsavelDto resp = new ResponsavelDto();
            resp.setTitularTitulo("TITULAR");
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));
            
            UsuarioDto titular = new UsuarioDto(); // Sem email
            when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", titular));

            listener.aoIniciarProcesso(new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of()));

            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve ignorar envio se tipo de unidade desconhecido")
        void deveIgnorarTipoDesconhecido() {
            Processo processo = new Processo();
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            Unidade u = new Unidade();
            u.setCodigo(10L);
            u.setTipo(TipoUnidade.RAIZ); // Não tratado no switch/if

            Subprocesso sp = new Subprocesso();
            sp.setUnidade(u);

            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

            ResponsavelDto resp = new ResponsavelDto();
            resp.setTitularTitulo("TITULAR");
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));

            UsuarioDto titular = new UsuarioDto();
            titular.setEmail("t@mail.com");
            when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", titular));

            listener.aoIniciarProcesso(new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of()));

            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve ignorar se subprocesso sem unidade")
        void deveIgnorarSemUnidade() {
            Processo p = new Processo();
            Subprocesso sp = new Subprocesso();
            sp.setUnidade(null);

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

            listener.aoIniciarProcesso(new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of()));

            verify(usuarioService, never()).buscarResponsaveisUnidades(any());
        }
    }

    @Nested
    @DisplayName("Processo Finalizado")
    class FinalizacaoProcesso {
        @Test
        @DisplayName("Deve processar finalização com sucesso para unidade OPERACIONAL")
        void deveProcessarFinalizacaoOperacional() {
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setDescricao("P1");
            Unidade u = new Unidade();
            u.setCodigo(10L);
            u.setSigla("U1");
            u.setTipo(TipoUnidade.OPERACIONAL);
            p.setParticipantes(Set.of(u));

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
            
            ResponsavelDto resp = new ResponsavelDto();
            resp.setTitularTitulo("TITULAR");
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));
            
            UsuarioDto titular = new UsuarioDto();
            titular.setEmail("t@mail.com");
            when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", titular));

            when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("html");

            listener.aoFinalizarProcesso(new EventoProcessoFinalizado(1L, LocalDateTime.now()));

            verify(notificacaoEmailService).enviarEmailHtml(eq("t@mail.com"), any(), any());
        }

        @Test
        @DisplayName("Deve processar finalização com sucesso para unidade INTERMEDIARIA")
        void deveProcessarFinalizacaoIntermediaria() {
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setDescricao("P1");

            Unidade uPai = new Unidade();
            uPai.setCodigo(10L);
            uPai.setSigla("PAI");
            uPai.setTipo(TipoUnidade.INTERMEDIARIA);

            Unidade uFilha = new Unidade();
            uFilha.setCodigo(20L);
            uFilha.setSigla("FILHA");
            uFilha.setUnidadeSuperior(uPai);

            p.setParticipantes(Set.of(uPai, uFilha));

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);

            ResponsavelDto resp = new ResponsavelDto();
            resp.setTitularTitulo("TITULAR");
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp, 20L, resp));
            
            UsuarioDto titular = new UsuarioDto();
            titular.setEmail("t@mail.com");
            when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", titular));

            when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any()))
                    .thenReturn("html");

            listener.aoFinalizarProcesso(new EventoProcessoFinalizado(1L, LocalDateTime.now()));

            // Verifica envio para a intermediária
            verify(notificacaoEmailService, atLeastOnce()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve ignorar envio para intermediária se não houver subordinadas")
        void deveIgnorarIntermediariaSemSubordinadas() {
            Processo p = new Processo();
            Unidade uPai = new Unidade();
            uPai.setCodigo(10L);
            uPai.setTipo(TipoUnidade.INTERMEDIARIA);
            p.setParticipantes(Set.of(uPai)); // Só ela participa

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);

            ResponsavelDto resp = new ResponsavelDto();
            resp.setTitularTitulo("TITULAR");
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));

            UsuarioDto titular = new UsuarioDto();
            titular.setEmail("t@mail.com");
            when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", titular));

            listener.aoFinalizarProcesso(new EventoProcessoFinalizado(1L, LocalDateTime.now()));

            // Não deve enviar pois a lista de siglas subordinadas será vazia
            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve ignorar se não houver participantes")
        void deveIgnorarSemParticipantes() {
            Processo p = new Processo();
            p.setParticipantes(Set.of());
            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);

            listener.aoFinalizarProcesso(new EventoProcessoFinalizado(1L, LocalDateTime.now()));

            verify(usuarioService, never()).buscarResponsaveisUnidades(any());
        }

        @Test
        @DisplayName("Deve capturar erro e não falhar na finalização")
        void deveCapturarErroFinalizacao() {
            when(processoService.buscarEntidadePorId(1L)).thenThrow(new RuntimeException("Erro"));
            
            assertThatCode(() -> listener.aoFinalizarProcesso(new EventoProcessoFinalizado(1L, LocalDateTime.now())))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com responsável não encontrado na finalização")
        void deveLidarFaltaResponsavelFinalizacao() {
            Processo p = new Processo();
            Unidade u = new Unidade();
            u.setCodigo(10L);
            p.setParticipantes(Set.of(u));

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Collections.emptyMap());

            listener.aoFinalizarProcesso(new EventoProcessoFinalizado(1L, LocalDateTime.now()));

            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve lidar com titular sem email na finalização")
        void deveLidarTitularSemEmailFinalizacao() {
            Processo p = new Processo();
            Unidade u = new Unidade();
            u.setCodigo(10L);
            p.setParticipantes(Set.of(u));

            when(processoService.buscarEntidadePorId(1L)).thenReturn(p);

            ResponsavelDto resp = new ResponsavelDto();
            resp.setTitularTitulo("TITULAR");
            when(usuarioService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, resp));

            UsuarioDto titular = new UsuarioDto(); // Sem email
            when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("TITULAR", titular));

            listener.aoFinalizarProcesso(new EventoProcessoFinalizado(1L, LocalDateTime.now()));

            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }
    }
}
