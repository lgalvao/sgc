package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import sgc.notificacao.NotificacaoEmailService;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        service.enviarEmailTransicao(evento);
    }
}
