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
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
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
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa1(LocalDateTime.now());

        Unidade dest = new Unidade();
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DEVOLVIDO, new Unidade(), dest, "Motivo");

        verify(notificacaoEmailService).enviarEmail(eq("DEST"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Notifica hierarquia")
    void notificaHierarquia() {
        Subprocesso sp = criarSubprocesso();

        Unidade origem = new Unidade();
        origem.setSigla("ORIG");
        Unidade superior = new Unidade();
        superior.setSigla("SUP");
        origem.setUnidadeSuperior(superior);

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, origem, new Unidade(), null);

        // Verifica envio para superior
        verify(notificacaoEmailService).enviarEmail(eq("SUP"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Lida com exceção ao enviar email")
    void lidaComExcecao() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any())).thenThrow(new RuntimeException("Template error"));

        // Should not throw
        assertThatCode(() -> service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, null, new Unidade(), null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não envia email se tipo de transição não exige")
    void naoEnviaSeTipoNaoExige() {
        service.enviarEmailTransicaoDireta(null, TipoTransicao.CADASTRO_HOMOLOGADO, null, null, null);

        verifyNoInteractions(notificacaoEmailService);
        verifyNoInteractions(templateEngine);
    }

    @Test
    @DisplayName("Não envia email se destinatário nulo")
    void naoEnviaSeDestinoNulo() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, null, null, null);

        verify(notificacaoEmailService, never()).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Não notifica hierarquia se origem nula")
    void naoNotificaHierarquiaSemOrigem() {
        Subprocesso sp = criarSubprocesso();
        Unidade destino = new Unidade();
        destino.setSigla("DEST");

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, null, destino, null);

        // Envia para destino normal
        verify(notificacaoEmailService, times(1)).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Trata exceção ao notificar hierarquia")
    void trataExcecaoHierarquia() {
        Subprocesso sp = criarSubprocesso();
        sp.getUnidade().setSigla("U1");

        Unidade origem = new Unidade();
        origem.setSigla("ORIG");
        Unidade superior = new Unidade();
        superior.setSigla("SUP");
        origem.setUnidadeSuperior(superior);

        Unidade destino = new Unidade();
        destino.setSigla("DEST");

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        doNothing().when(notificacaoEmailService).enviarEmail(eq("DEST"), anyString(), any());
        doThrow(new RuntimeException("Fail")).when(notificacaoEmailService).enviarEmail(eq("SUP"), anyString(), any());

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, origem, destino, null);

        verify(notificacaoEmailService).enviarEmail(eq("SUP"), anyString(), any());
        verify(notificacaoEmailService).enviarEmail(eq("DEST"), anyString(), any());
    }

    @Test
    @DisplayName("Cria variáveis sem datas nem observações")
    void deveCriarVariaveisSemDatasNemObservacoes() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa1(null);
        sp.setDataLimiteEtapa2(null);

        when(templateEngine.process(anyString(), any())).thenReturn("html");
        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), new Unidade(), null);
        verify(notificacaoEmailService).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Deve incluir DataLimiteEtapa2 nas variáveis")
    void deveIncluirDataLimiteEtapa2NasVariaveis() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), new Unidade(), null);

        verify(templateEngine).process(anyString(), argThat(ctx ->
                ctx.getVariable("dataLimiteEtapa2") != null
        ));
    }

    @Test
    @DisplayName("Deve incluir Observações nas variáveis")
    void deveIncluirObservacoesNasVariaveis() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), new Unidade(), "Minha Observação");

        verify(templateEngine).process(anyString(), argThat(ctx ->
                "Minha Observação".equals(ctx.getVariable("observacoes"))
        ));
    }

    @Test
    @DisplayName("Deve lidar com tipo processo iniciado (switch default)")
    void deveLidarComTipoProcessoIniciado() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.PROCESSO_INICIADO, new Unidade(), new Unidade(), null);

        // Verifica que enviou email com assunto formatado pelo default do switch
        verify(notificacaoEmailService).enviarEmail(any(),
                argThat(s -> s.contains("SGC: Notificação - Processo iniciado")),
                any());
    }

    // ============================================================================================
    // MÉTODO AUXILIAR
    // ============================================================================================

    private Subprocesso criarSubprocesso() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setNome("Unidade 1");
        return sp;
    }
}
