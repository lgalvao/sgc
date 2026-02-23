package sgc.subprocesso.service.notificacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import sgc.notificacao.EmailService;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoEmailService Test")
class SubprocessoEmailServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private sgc.organizacao.UnidadeFacade unidadeFacade;
    @Mock
    private sgc.organizacao.UsuarioFacade usuarioFacade;

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

        verify(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), anyString(), eq("html"));
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

        Unidade destino = new Unidade();
        destino.setCodigo(1L);
        destino.setSigla("DEST");

        // Mock responsavel para evitar NPE
        when(unidadeFacade.buscarResponsavelUnidade(1L)).thenReturn(mock(UnidadeResponsavelDto.class));

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, origem, destino, null);

        // Verifica envio para superior
        verify(emailService).enviarEmailHtml(eq("sup@tre-pe.jus.br"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Lida com exceção ao enviar email")
    void lidaComExcecao() {
        Subprocesso sp = criarSubprocesso();

        Unidade dest = new Unidade();
        dest.setSigla("DEST");
        
        // Ensure template engine is called, so exception is thrown in the try block
        lenient().when(templateEngine.process(anyString(), any())).thenThrow(new RuntimeException("Template error"));

        assertThatCode(() -> service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, null, dest, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não envia email se tipo de transição não exige")
    void naoEnviaSeTipoNaoExige() {
        service.enviarEmailTransicaoDireta(null, TipoTransicao.CADASTRO_HOMOLOGADO, null, null, null);

        verifyNoInteractions(emailService);
        verifyNoInteractions(templateEngine);
    }

    @Test
    @DisplayName("Não envia email se destinatário nulo")
    void naoEnviaSeDestinoNulo() {
        Subprocesso sp = criarSubprocesso();
        
        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, null, null, null);

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Não notifica hierarquia se origem nula")
    void naoNotificaHierarquiaSemOrigem() {
        Subprocesso sp = criarSubprocesso();
        Unidade destino = new Unidade();
        destino.setSigla("DEST");

        lenient().when(templateEngine.process(anyString(), any())).thenReturn("html");

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, null, destino, null);

        // Logs error because variable creation fails with null origin, so no email sent
        verifyNoInteractions(emailService);
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
        destino.setCodigo(1L);
        destino.setSigla("DEST");

        // Mock responsavel para evitar NPE
        lenient().when(unidadeFacade.buscarResponsavelUnidade(1L)).thenReturn(mock(UnidadeResponsavelDto.class));

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        doNothing().when(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), anyString(), any());
        doThrow(new RuntimeException("Fail")).when(emailService).enviarEmailHtml(eq("sup@tre-pe.jus.br"), anyString(), any());

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, origem, destino, null);

        verify(emailService).enviarEmailHtml(eq("sup@tre-pe.jus.br"), anyString(), any());
        verify(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Cria variáveis sem datas nem observações")
    void deveCriarVariaveisSemDatasNemObservacoes() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa1(null);
        sp.setDataLimiteEtapa2(null);

        Unidade orig = new Unidade(); orig.setSigla("O");
        Unidade dest = new Unidade(); dest.setSigla("D");
        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, orig, dest, null);
        verify(emailService).enviarEmailHtml(eq("d@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Deve incluir DataLimiteEtapa2 nas variáveis")
    void deveIncluirDataLimiteEtapa2NasVariaveis() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        Unidade dest = new Unidade(); dest.setSigla("D");
        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), dest, null);

        verify(templateEngine).process(anyString(), argThat(ctx -> ctx.getVariable("dataLimiteEtapa2") != null
        ));
    }

    @Test
    @DisplayName("Deve incluir Observações nas variáveis")
    void deveIncluirObservacoesNasVariaveis() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        Unidade dest = new Unidade(); dest.setSigla("D");
        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), dest, "Minha Observação");

        verify(templateEngine).process(anyString(), argThat(ctx ->
                "Minha Observação".equals(ctx.getVariable("observacoes"))
        ));
    }

    @Test
    @DisplayName("Deve lidar com tipo processo iniciado (switch default)")
    void deveLidarComTipoProcessoIniciado() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        Unidade dest = new Unidade(); dest.setSigla("D");
        service.enviarEmailTransicaoDireta(sp, TipoTransicao.PROCESSO_INICIADO, new Unidade(), dest, null);

        // Verifica que enviou email com assunto formatado pelo default do switch
        verify(emailService).enviarEmailHtml(any(),
                argThat(s -> s != null && s.contains("SGC: Notificação - Processo iniciado")),
                any());
    }

    @Test
    @DisplayName("Deve enviar email para substituto se houver")
    void deveEnviarEmailParaSubstitutoSeHouver() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = new Unidade();
        dest.setCodigo(1L);
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .substitutoTitulo("123456")
                .build();
        when(unidadeFacade.buscarResponsavelUnidade(1L)).thenReturn(resp);

        Usuario substituto = new Usuario();
        substituto.setEmail("sub@teste.com");
        when(usuarioFacade.buscarUsuarioPorTitulo("123456")).thenReturn(Optional.of(substituto));

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), dest, null);

        verify(emailService).enviarEmailHtml(eq("sub@teste.com"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Não deve enviar email para substituto se email vazio")
    void naoDeveEnviarEmailParaSubstitutoSeEmailVazio() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = new Unidade();
        dest.setCodigo(1L);
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any())).thenReturn("html");

        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .substitutoTitulo("123456")
                .build();
        when(unidadeFacade.buscarResponsavelUnidade(1L)).thenReturn(resp);

        Usuario substituto = new Usuario();
        substituto.setEmail(""); // Email vazio
        when(usuarioFacade.buscarUsuarioPorTitulo("123456")).thenReturn(Optional.of(substituto));

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), dest, null);

        // Verifica envio para a unidade (padrão), mas NÃO para o substituto
        verify(emailService, times(1)).enviarEmailHtml(anyString(), anyString(), anyString());
        verify(emailService, never()).enviarEmailHtml(eq(""), anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve enviar email pessoal se responsavel for null")
    void naoDeveEnviarEmailPessoalSeResponsavelNull() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = new Unidade();
        dest.setCodigo(1L);
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any())).thenReturn("html");
        when(unidadeFacade.buscarResponsavelUnidade(1L)).thenReturn(null);

        service.enviarEmailTransicaoDireta(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Unidade(), dest, null);

        // Apenas o email da unidade deve ser enviado
        verify(emailService, times(1)).enviarEmailHtml(anyString(), anyString(), anyString());
    }

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
