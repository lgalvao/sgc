package sgc.subprocesso.service.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoComunicacaoListenerTest {
    @Mock private AlertaFacade alertaFacade;
    @Mock private SubprocessoEmailService emailService;

    @InjectMocks
    private SubprocessoComunicacaoListener listener;

    @Test
    @DisplayName("Deve gerar alerta e enviar email para CADASTRO_DISPONIBILIZADO")
    void deveGerarAlertaEEnviarEmail() {
        Subprocesso subprocesso = new Subprocesso();
        Unidade unidade = new Unidade();
        unidade.setSigla("SIGLA");
        subprocesso.setUnidade(unidade);
        Processo processo = new Processo();
        subprocesso.setProcesso(processo);
        
        Unidade origem = new Unidade();
        Unidade destino = new Unidade();
        
        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .build();

        listener.handle(evento);

        verify(alertaFacade).criarAlertaTransicao(eq(processo), anyString(), eq(origem), eq(destino));
        verify(emailService).enviarEmailTransicao(evento);
    }

    @Test
    @DisplayName("Não deve gerar alerta nem enviar email para CADASTRO_HOMOLOGADO")
    void naoDeveGerarNada() {
        Subprocesso subprocesso = new Subprocesso();
        Unidade unidade = new Unidade();
        unidade.setSigla("SIGLA");
        subprocesso.setUnidade(unidade);
        
        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(subprocesso)
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .build();

        listener.handle(evento);

        verify(alertaFacade, never()).criarAlertaTransicao(any(), anyString(), any(), any());
        verify(emailService, never()).enviarEmailTransicao(any());
    }
}