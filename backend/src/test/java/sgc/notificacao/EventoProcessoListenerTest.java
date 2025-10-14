package sgc.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaService;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerTest {
    private static final String UNID_OP = "UNID-OP";
    private static final Long T123 = 123123123123L;
    private static final String TITULAR_TESTE = "Titular Teste";
    private static final String TITULAR_EMAIL = "titular@test.com";
    private static final String RAMAL = "12345";
    private static final String ANALISTA = "Analista";
    private static final Long S456 = 456456456456L;
    private static final String SUBSTITUTO_TESTE = "Substituto Teste";
    private static final String SUBSTITUTO_EMAIL = "substituto@test.com";
    private static final String RAMAL_SUBSTITUTO = "67890";
    private static final String TECNICO = "Tecnico";
    @Mock
    private AlertaService alertaService;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private NotificacaoModeloEmailService notificacaoModeloEmailService;

    @Mock
    private SgrhService sgrhService;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private EventoProcessoListener ouvinteDeEvento;

    private Processo processo;
    private Subprocesso subprocessoOperacional;
    private ProcessoIniciadoEvento evento;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Teste de Processo");
        processo.setTipo(TipoProcesso.REVISAO);

        Unidade unidadeOperacional = new Unidade();
        unidadeOperacional.setCodigo(100L);
        unidadeOperacional.setSigla(UNID_OP);

        subprocessoOperacional = new Subprocesso();
        subprocessoOperacional.setCodigo(10L);
        subprocessoOperacional.setProcesso(processo);
        subprocessoOperacional.setUnidade(unidadeOperacional);
        subprocessoOperacional.setDataLimiteEtapa1(LocalDate.now().plusDays(10));

        evento = new ProcessoIniciadoEvento(1L, "INICIADO", LocalDateTime.now(), List.of(100L));
    }

    @Test
    @DisplayName("Deve processar evento, criar alertas e enviar e-mails para unidade operacional")
    void aoIniciarProcesso_deveProcessarCompleto_quandoUnidadeOperacional() {
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));

        UnidadeDto unidadeDto = new UnidadeDto(100L, "Unidade Operacional", UNID_OP, null, "OPERACIONAL");
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));

        ResponsavelDto responsavelDto = new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, String.valueOf(S456), SUBSTITUTO_TESTE);
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));

        UsuarioDto titular = new UsuarioDto(String.valueOf(T123), TITULAR_TESTE, TITULAR_EMAIL, RAMAL, ANALISTA);
        UsuarioDto substituto = new UsuarioDto(String.valueOf(S456), SUBSTITUTO_TESTE, SUBSTITUTO_EMAIL, RAMAL_SUBSTITUTO, TECNICO);
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123))).thenReturn(Optional.of(titular));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(S456))).thenReturn(Optional.of(substituto));

        when(notificacaoModeloEmailService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                .thenReturn("<html><body>Email Operacional</body></html>");

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, times(1)).criarAlertasProcessoIniciado(processo, List.of(subprocessoOperacional.getUnidade().getCodigo()), List.of(subprocessoOperacional));

        verify(notificacaoService, times(1)).enviarEmailHtml(
                eq(TITULAR_EMAIL),
                anyString(),
                contains("Email Operacional")
        );
        verify(notificacaoService, times(1)).enviarEmailHtml(
                eq(SUBSTITUTO_EMAIL),
                anyString(),
                contains("Email Operacional")
        );
    }

    @Test
    @DisplayName("Não deve fazer nada se o processo não for encontrado")
    void aoIniciarProcesso_naoDeveFazerNada_quandoProcessoNaoEncontrado() {
        when(processoRepo.findById(1L)).thenReturn(Optional.empty());

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), anyList(), anyList());
        verify(notificacaoService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mails se não houver subprocessos")
    void aoIniciarProcesso_naoDeveEnviarEmails_quandoNaoHouverSubprocessos() {
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), anyList(), anyList());
        verify(notificacaoService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve enviar e-mail correto para unidade INTERMEDIARIA")
    void aoIniciarProcesso_deveEnviarEmailCorreto_quandoUnidadeIntermediaria() {
        UnidadeDto unidadeDto = new UnidadeDto(100L, "Unidade Intermediaria", "UNID-INT", null, "INTERMEDIARIA");
        ResponsavelDto responsavelDto = new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null); // Sem substituto
        UsuarioDto titular = new UsuarioDto(String.valueOf(T123), TITULAR_TESTE, TITULAR_EMAIL, RAMAL, ANALISTA);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123))).thenReturn(Optional.of(titular));
        when(notificacaoModeloEmailService.criarTemplateBase(anyString(), anyString()))
                .thenReturn("<html><body>Email Intermediaria</body></html>");

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, times(1)).criarAlertasProcessoIniciado(any(), anyList(), anyList());
        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoService, times(1)).enviarEmailHtml(
                eq(TITULAR_EMAIL),
                assuntoCaptor.capture(),
                corpoCaptor.capture()
        );

        assertEquals("Processo Iniciado em Unidades Subordinadas - Teste de Processo", assuntoCaptor.getValue());
        assertTrue(corpoCaptor.getValue().contains("Email Intermediaria"));
        verify(notificacaoService, never()).enviarEmailHtml(eq(SUBSTITUTO_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve enviar e-mail correto para unidade INTEROPERACIONAL")
    void aoIniciarProcesso_deveEnviarEmailCorreto_quandoUnidadeInteroperacional() {
        UnidadeDto unidadeDto = new UnidadeDto(100L, "Unidade Interoperacional", "UNID-IO", null, "INTEROPERACIONAL");
        ResponsavelDto responsavelDto = new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null);
        UsuarioDto titular = new UsuarioDto(String.valueOf(T123), TITULAR_TESTE, TITULAR_EMAIL, RAMAL, ANALISTA);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123))).thenReturn(Optional.of(titular));
        when(notificacaoModeloEmailService.criarTemplateBase(anyString(), anyString()))
                .thenReturn("<html><body>Email Interoperacional</body></html>");

        ouvinteDeEvento.aoIniciarProcesso(evento);

        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoService).enviarEmailHtml(eq(TITULAR_EMAIL), assuntoCaptor.capture(), contains("Email Interoperacional"));
        assertEquals("Processo Iniciado - Teste de Processo", assuntoCaptor.getValue());
    }

    @Test
    @DisplayName("Não deve enviar e-mail para tipo de unidade desconhecido")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoTipoUnidadeDesconhecido() {
        UnidadeDto unidadeDto = new UnidadeDto(100L, "Unidade Desconhecida", "UNID-DESC", null, "DESCONHECIDO");
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(notificacaoService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se subprocesso não tiver unidade")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoSubprocessoSemUnidade() {
        subprocessoOperacional.setUnidade(null);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocessoOperacional));

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(sgrhService, never()).buscarUnidadePorCodigo(any());
        verify(notificacaoService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se responsável da unidade não for encontrado")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoResponsavelNaoEncontrado() {
        UnidadeDto unidadeDto = new UnidadeDto(100L, "Unidade Operacional", "UNID-OP", null, "OPERACIONAL");
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.empty());

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(notificacaoService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se titular não tiver e-mail")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoTitularSemEmail() {
        UnidadeDto unidadeDto = new UnidadeDto(100L, "Unidade Operacional", UNID_OP, null, "OPERACIONAL");
        ResponsavelDto responsavelDto = new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null);
        UsuarioDto titularSemEmail = new UsuarioDto(String.valueOf(T123), TITULAR_TESTE, " ", RAMAL, ANALISTA); // Email em branco

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123))).thenReturn(Optional.of(titularSemEmail));

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(notificacaoService, never()).enviarEmailHtml(any(), any(), any());
    }
}
