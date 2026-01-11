package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import sgc.notificacao.NotificacaoEmailService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoEmailService Test")
class SubprocessoEmailServiceTest {

    @Mock
    private NotificacaoEmailService notificacaoEmailService;
    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private SubprocessoEmailService service;

    @Test
    @DisplayName("Envia email para unidade destino")
    void enviaEmailDestino() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setNome("Unidade 1");
        sp.setDataLimiteEtapa1(LocalDateTime.now());

        Unidade dest = new Unidade();
        dest.setSigla("DEST");

        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(sp)
                .tipo(TipoTransicao.CADASTRO_DEVOLVIDO)
                .usuario(new Usuario())
                .unidadeOrigem(new Unidade())
                .unidadeDestino(dest)
                .observacoes("Motivo")
                .build();

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicao(evento);

        verify(notificacaoEmailService).enviarEmail(eq("DEST"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Notifica hierarquia")
    void notificaHierarquia() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");

        Unidade origem = new Unidade();
        origem.setSigla("ORIG");
        Unidade superior = new Unidade();
        superior.setSigla("SUP");
        origem.setUnidadeSuperior(superior);

        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .usuario(new Usuario())
                .unidadeOrigem(origem)
                .unidadeDestino(new Unidade())
                .build();

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicao(evento);

        // Verifica envio para superior
        verify(notificacaoEmailService).enviarEmail(eq("SUP"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Lida com exceção ao enviar email")
    void lidaComExcecao() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);

        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
            .subprocesso(sp)
            .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
            .usuario(new Usuario())
            .unidadeDestino(new Unidade())
            .build();

        when(templateEngine.process(anyString(), any())).thenThrow(new RuntimeException("Template error"));

        // Should not throw
        assertThatCode(() -> service.enviarEmailTransicao(evento))
            .doesNotThrowAnyException();
    }
    @Test
    @DisplayName("Não envia email se tipo de transição não exige")
    void naoEnviaSeTipoNaoExige() {
        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO) // enviaEmail() == false
                .build();
        
        service.enviarEmailTransicao(evento);
        
        verifyNoInteractions(notificacaoEmailService);
        verifyNoInteractions(templateEngine);
    }
    
    @Test
    @DisplayName("Não envia email se destinatário nulo")
    void naoEnviaSeDestinoNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());

        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO) // enviaEmail() == true
                .unidadeDestino(null) // DESTINO NULO
                .build();

        when(templateEngine.process(anyString(), any())).thenReturn("html");
        
        service.enviarEmailTransicao(evento);

        verify(notificacaoEmailService, never()).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Não notifica hierarquia se origem nula")
    void naoNotificaHierarquiaSemOrigem() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());

        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO) // Notifica hierarquia
                .unidadeOrigem(null) // SEM ORIGEM
                .unidadeDestino(new Unidade()) // Com destino normal
                .build();
        
        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicao(evento);
        
        // Envia para destino normal
        verify(notificacaoEmailService, times(1)).enviarEmail(any(), any(), any());
    }
    
    @Test
    @DisplayName("Trata exceção ao notificar hierarquia")
    void trataExcecaoHierarquia() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1"); // Pra evitar null no assunto
        
        Unidade origem = new Unidade();
        origem.setSigla("ORIG");
        Unidade superior = new Unidade();
        superior.setSigla("SUP");
        origem.setUnidadeSuperior(superior);
        
        Unidade destino = new Unidade();
        destino.setSigla("DEST");

        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build();
        
        when(templateEngine.process(anyString(), any())).thenReturn("html");
        
        // Configura mocks explicites para cada chamada
        doNothing().when(notificacaoEmailService).enviarEmail(eq("DEST"), anyString(), any());
        doThrow(new RuntimeException("Fail")).when(notificacaoEmailService).enviarEmail(eq("SUP"), anyString(), any());
        
        // Deve continuar e não lançar exceção
        service.enviarEmailTransicao(evento);
        
        // Verifica que tentou enviar (e falhou, mas capturou)
        verify(notificacaoEmailService).enviarEmail(eq("SUP"), anyString(), any());
        // Verifica que enviou para destino também
        verify(notificacaoEmailService).enviarEmail(eq("DEST"), anyString(), any());
    }
}
