package sgc.notificacao;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerTest {

    @InjectMocks
    private EventoProcessoListener listener;

    @Mock private AlertaService servicoAlertas;
    @Mock private NotificacaoEmailService notificacaoEmailService;
    @Mock private NotificacaoModelosService notificacaoModelosService;
    @Mock private UsuarioService usuarioService;
    @Mock private ProcessoService processoService;
    @Mock private SubprocessoService subprocessoService;

    @Test
    void deveTratarExcecaoAoEnviarEmailProcessoIniciado() {
        // Mock Processo
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        
        when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);

        // Mock Subprocesso
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade Teste");
        unidade.setSigla("UT");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());

        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        // Mock Responsaveis
        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("123456")
                .substitutoTitulo("654321")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        // Mock Usuarios
        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("123456").email("titular@email.com").build();
        UsuarioDto substituto = UsuarioDto.builder().tituloEleitoral("654321").email("substituto@email.com").build();
        
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "123456", titular,
                "654321", substituto
        ));

        // Simula exceção ao enviar email (para titular ou substituto)
        doThrow(new RuntimeException("Erro envio email"))
            .when(notificacaoEmailService).enviarEmailHtml(eq("titular@email.com"), anyString(), any());

        // Executa
        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        // Verifica que continuou (não explodiu) e logou o erro (implicitamente, pelo coverage da catch)
        verify(notificacaoEmailService).enviarEmailHtml(eq("titular@email.com"), anyString(), any());
    }

    @Test
    void deveTratarExcecaoAoEnviarEmailParaSubstituto() {
       // Mock Processo e Subprocesso similar ao anterior
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);

        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        // Configura substituto
        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("111")
                .substitutoTitulo("222")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("111").email("t@t.com").build();
        UsuarioDto substituto = UsuarioDto.builder().tituloEleitoral("222").email("s@s.com").build(); // Email ok

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("111", titular, "222", substituto));

        // Força erro especificamente na chamada do substituto
        // Primeiro envia titular (ok)
        doNothing().when(notificacaoEmailService).enviarEmailHtml(eq("t@t.com"), anyString(), any());
        // Segundo envia substituto (erro)
        doThrow(new RuntimeException("Erro substituto"))
                .when(notificacaoEmailService).enviarEmailHtml(eq("s@s.com"), anyString(), any());

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        // Deve ter chamado ambos
        verify(notificacaoEmailService).enviarEmailHtml(eq("t@t.com"), anyString(), any());
        verify(notificacaoEmailService).enviarEmailHtml(eq("s@s.com"), anyString(), any());
    }

    @Test
    void deveIgnorarSubstitutoSeDadosInvalidos() {
       Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("P1");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        subprocesso.setUnidade(unidade);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("111")
                .substitutoTitulo("222") // Tem substituto definido na responsabilidade
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("111").email("t@t.com").build();
        // Substituto com email nulo
        UsuarioDto substituto = UsuarioDto.builder().tituloEleitoral("222").email(null).build();

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("111", titular, "222", substituto));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        // Deve enviar para titular, mas NÃO para substituto (e não deve lançar erro)
        verify(notificacaoEmailService).enviarEmailHtml(eq("t@t.com"), anyString(), any());
        verify(notificacaoEmailService, never()).enviarEmailHtml(isNull(), anyString(), any());
    }

    @Test
    void deveEnviarParaInteroperacional() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("P1");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(20L);
        unidade.setNome("U Inter");
        unidade.setTipo(TipoUnidade.INTEROPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(200L);
        subprocesso.setUnidade(unidade);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder().unidadeCodigo(20L).titularTitulo("333").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(20L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("333").email("inter@t.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("333", titular));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();
        listener.aoIniciarProcesso(evento);

        verify(notificacaoEmailService).enviarEmailHtml(eq("inter@t.com"), contains("Processo Iniciado"), any());
    }
    
    @Test
    void deveEnviarParaIntermediariaComSubordinadas() {
        // Teste de finalização para intermediária (cobre 189-194)
        Processo processo = new Processo();
        processo.setCodigo(2L);
        processo.setDescricao("P2");
        when(processoService.buscarEntidadePorId(2L)).thenReturn(processo);
        
        Unidade intermediaria = new Unidade();
        intermediaria.setCodigo(30L);
        intermediaria.setSigla("INTER");
        intermediaria.setTipo(TipoUnidade.INTERMEDIARIA);
        
        Unidade subordinada = new Unidade();
        subordinada.setCodigo(40L);
        subordinada.setSigla("SUB");
        subordinada.setUnidadeSuperior(intermediaria);
        
        // Ambas participam so que notificamos a intermediaria sobre a finalização (e ela ve as subordinadas)
        processo.setParticipantes(Set.of(intermediaria, subordinada));
        
        // Mock responsaveis
        ResponsavelDto respInter = ResponsavelDto.builder().unidadeCodigo(30L).titularTitulo("444").build();
        ResponsavelDto respSub = ResponsavelDto.builder().unidadeCodigo(40L).titularTitulo("555").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(30L, respInter, 40L, respSub));
        
        UsuarioDto userInter = UsuarioDto.builder().tituloEleitoral("444").email("inter@mail.com").build();
        UsuarioDto userSub = UsuarioDto.builder().tituloEleitoral("555").email("sub@mail.com").build();
        
        when(usuarioService.buscarUsuariosPorTitulos(argThat(list -> list.containsAll(List.of("444", "555")))))
            .thenReturn(Map.of("444", userInter, "555", userSub));
        
        // Executa
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(2L).build();
        listener.aoFinalizarProcesso(evento);
        
        // Verifica envio para intermediária com assunto específico
        verify(notificacaoEmailService).enviarEmailHtml(eq("inter@mail.com"), contains("unidades subordinadas"), any());
    }

    @Test
    void naoDeveEnviarParaIntermediariaSemSubordinadas() {
        // Teste de finalização para intermediária SEM subordinadas no processo (cobre 184)
        Processo processo = new Processo();
        processo.setCodigo(3L);
        processo.setDescricao("P3");
        when(processoService.buscarEntidadePorId(3L)).thenReturn(processo);
        
        Unidade intermediaria = new Unidade();
        intermediaria.setCodigo(50L);
        intermediaria.setSigla("INTER_SOLO");
        intermediaria.setTipo(TipoUnidade.INTERMEDIARIA);
        
        // Apenas a intermediaria participa (ou subordinadas não estao no .getParticipantes())
        processo.setParticipantes(Set.of(intermediaria));
        
        ResponsavelDto respInter = ResponsavelDto.builder().unidadeCodigo(50L).titularTitulo("666").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(50L, respInter));
        
        UsuarioDto userInter = UsuarioDto.builder().tituloEleitoral("666").email("solo@mail.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("666", userInter));
        
        // Executa
        EventoProcessoFinalizado evento = EventoProcessoFinalizado.builder().codProcesso(3L).build();
        listener.aoFinalizarProcesso(evento);
        
        // NAO deve enviar email para a intermediaria pois não tem subordinadas no contexto
        verify(notificacaoEmailService, never()).enviarEmailHtml(eq("solo@mail.com"), anyString(), any());
    }

    @Test
    void deveLogarErroParaTipoUnidadeNaoSuportado() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("P1");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);

        Unidade unidade = new Unidade();
        unidade.setCodigo(99L);
        unidade.setTipo(TipoUnidade.RAIZ); // Tipo não suportado no switch

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(999L);
        subprocesso.setUnidade(unidade);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder().unidadeCodigo(99L).titularTitulo("999").build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(99L, responsavel));

        UsuarioDto titular = UsuarioDto.builder().tituloEleitoral("999").email("raiz@mail.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("999", titular));

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(1L).build();

        // Não deve lançar exceção para fora, mas deve logar o erro (capturado pelo try/catch interno)
        listener.aoIniciarProcesso(evento);

        // Verifica que NÃO enviou e-mail
        verify(notificacaoEmailService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    void deveEnviarEmailParaUnidadeIntermediaria() {
        // Testa envio de email específico para unidade INTERMEDIARIA ao iniciar processo
        Processo processo = new Processo();
        processo.setCodigo(5L);
        processo.setDescricao("Processo com Intermediária");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoService.buscarEntidadePorId(5L)).thenReturn(processo);

        Unidade intermediaria = new Unidade();
        intermediaria.setCodigo(100L);
        intermediaria.setSigla("INTER");
        intermediaria.setNome("Unidade Intermediária");
        intermediaria.setTipo(TipoUnidade.INTERMEDIARIA);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(200L);
        subprocesso.setUnidade(intermediaria);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));

        when(subprocessoService.listarEntidadesPorProcesso(5L)).thenReturn(List.of(subprocesso));

        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(100L)
                .titularTitulo("888")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(100L, responsavel));

        UsuarioDto titular = UsuarioDto.builder()
                .tituloEleitoral("888")
                .email("inter@email.com")
                .build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("888", titular));

        when(notificacaoModelosService.criarEmailProcessoIniciado(anyString(), anyString(), anyString(), any()))
                .thenReturn("<html>Email content</html>");

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(5L).build();
        listener.aoIniciarProcesso(evento);

        // Verifica que enviou email com assunto específico para INTERMEDIARIA
        verify(notificacaoEmailService).enviarEmailHtml(
                eq("inter@email.com"),
                contains("Unidades Subordinadas"),
                anyString()
        );
    }

    @Test
    void deveTratarExcecaoNoLoopDeSubprocessos() {
        // Testa o catch do loop externo (linhas 123-124) quando enviarEmailProcessoIniciado lança exceção
        Processo processo = new Processo();
        processo.setCodigo(6L);
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        when(processoService.buscarEntidadePorId(6L)).thenReturn(processo);

        // Primeiro subprocesso normal
        Subprocesso subprocessoBom = new Subprocesso();
        subprocessoBom.setCodigo(300L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(50L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setNome("Unidade Normal");
        unidade.setSigla("UN");
        subprocessoBom.setUnidade(unidade);
        subprocessoBom.setDataLimiteEtapa1(LocalDateTime.now());

        // Segundo subprocesso que causará exceção devido a responsável null no map
        Subprocesso subprocessoRuim = new Subprocesso();
        subprocessoRuim.setCodigo(301L);
        Unidade unidadeRuim = new Unidade();
        unidadeRuim.setCodigo(99L); // Código que não terá responsável no map
        unidadeRuim.setTipo(TipoUnidade.OPERACIONAL);
        subprocessoRuim.setUnidade(unidadeRuim);

        when(subprocessoService.listarEntidadesPorProcesso(6L))
                .thenReturn(List.of(subprocessoBom, subprocessoRuim));

        // Fornece responsável apenas para a unidade 50, não para 99
        ResponsavelDto responsavel = ResponsavelDto.builder()
                .unidadeCodigo(50L)
                .titularTitulo("777")
                .build();
        when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(50L, responsavel));

        UsuarioDto titular = UsuarioDto.builder()
                .tituloEleitoral("777")
                .email("normal@email.com")
                .build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("777", titular));

        when(notificacaoModelosService.criarEmailProcessoIniciado(anyString(), anyString(), anyString(), any()))
                .thenReturn("<html>Email</html>");

        EventoProcessoIniciado evento = EventoProcessoIniciado.builder().codProcesso(6L).build();
        
        // Não deve lançar exceção para fora - deve capturar e logar
        listener.aoIniciarProcesso(evento);

        // Verifica que o primeiro subprocesso foi processado com sucesso
        // O segundo causará NPE ao tentar acessar responsavel.getTitularTitulo() quando responsavel é null
        verify(notificacaoEmailService).enviarEmailHtml(eq("normal@email.com"), anyString(), anyString());
    }
}
