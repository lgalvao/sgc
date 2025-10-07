package sgc.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaService;
import sgc.processo.EventoProcessoIniciado;
import sgc.processo.Processo;
import sgc.processo.ProcessoRepository;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoEventListenerTest {

    @Mock
    private AlertaService alertaService;
    @Mock
    private EmailNotificationService emailService;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private SgrhService sgrhService;
    @Mock
    private ProcessoRepository processoRepository;
    @Mock
    private SubprocessoRepository subprocessoRepository;

    @InjectMocks
    private ProcessoEventListener eventListener;

    @Captor
    private ArgumentCaptor<String> emailBodyCaptor;

    private Processo processo;
    private Subprocesso subprocesso;
    private UnidadeDto unidadeDto;
    private ResponsavelDto responsavelDto;
    private UsuarioDto titularDto;
    private UsuarioDto substitutoDto;
    private EventoProcessoIniciado evento;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo de Teste");
        processo.setTipo("AVALIAÇÃO");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDate.of(2024, 12, 31));

        unidadeDto = new UnidadeDto(10L, "UNIDADE TESTE", "UT", 1L, "OPERACIONAL");
        responsavelDto = new ResponsavelDto(10L, "TITULAR123", "Fulano", "SUBSTITUTO456", "Ciclano");
        titularDto = new UsuarioDto("TITULAR123", "Fulano", "fulano@email.com", "F123", "Analista");
        substitutoDto = new UsuarioDto("SUBSTITUTO456", "Ciclano", "ciclano@email.com", "C456", "Técnico");

        evento = new EventoProcessoIniciado(1L, "AVALIAÇÃO", LocalDateTime.now(), List.of(10L));
    }

    private void mockFullSuccessPath(String tipoUnidade) {
        unidadeDto = new UnidadeDto(10L, "UNIDADE TESTE", "UT", 1L, tipoUnidade);
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocesso));
        when(sgrhService.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo("TITULAR123")).thenReturn(Optional.of(titularDto));
        when(sgrhService.buscarUsuarioPorTitulo("SUBSTITUTO456")).thenReturn(Optional.of(substitutoDto));
    }

    @Test
    @DisplayName("Deve processar evento, criar alertas e enviar e-mails para titular e substituto")
    void handleProcessoIniciado_FullSuccessPath() {
        mockFullSuccessPath("OPERACIONAL");
        when(emailTemplateService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("HTML Body Operacional");

        eventListener.handleProcessoIniciado(evento);

        verify(alertaService).criarAlertasProcessoIniciado(processo, List.of(subprocesso));
        verify(emailService, times(2)).enviarEmailHtml(anyString(), anyString(), anyString());
        verify(emailService).enviarEmailHtml(eq("fulano@email.com"), anyString(), anyString());
        verify(emailService).enviarEmailHtml(eq("ciclano@email.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve fazer nada se o processo não for encontrado")
    void handleProcessoIniciado_ProcessoNotFound() {
        when(processoRepository.findById(anyLong())).thenReturn(Optional.empty());

        eventListener.handleProcessoIniciado(evento);

        verify(subprocessoRepository, never()).findByProcessoCodigoWithUnidade(any());
        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), any());
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve parar se não encontrar subprocessos")
    void handleProcessoIniciado_NoSubprocessos() {
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());

        eventListener.handleProcessoIniciado(evento);

        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), any());
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve enviar e-mail correto para unidade INTERMEDIARIA")
    void handleProcessoIniciado_UnidadeIntermediaria() {
        mockFullSuccessPath("INTERMEDIARIA");
        when(emailTemplateService.criarTemplateBase(anyString(), anyString())).thenCallRealMethod();

        eventListener.handleProcessoIniciado(evento);

        verify(emailService, times(2)).enviarEmailHtml(anyString(), anyString(), emailBodyCaptor.capture());
        String emailBody = emailBodyCaptor.getValue();
        assertTrue(emailBody.contains("Processo Iniciado em Unidades Subordinadas"));
        assertTrue(emailBody.contains("será possível visualizar e realizar a validação"));
    }

    @Test
    @DisplayName("Deve enviar e-mail correto para unidade INTEROPERACIONAL")
    void handleProcessoIniciado_UnidadeInteroperacional() {
        mockFullSuccessPath("INTEROPERACIONAL");
        when(emailTemplateService.criarTemplateBase(anyString(), anyString())).thenCallRealMethod();

        eventListener.handleProcessoIniciado(evento);

        verify(emailService, times(2)).enviarEmailHtml(anyString(), anyString(), emailBodyCaptor.capture());
        String emailBody = emailBodyCaptor.getValue();
        assertTrue(emailBody.contains("Unidade Interoperacional"));
        assertTrue(emailBody.contains("Você deverá realizar DUAS ações"));
    }

    @Test
    @DisplayName("Não deve enviar e-mail se o tipo de unidade for desconhecido")
    void handleProcessoIniciado_UnknownUnidadeType() {
        // Specific mocks for this path to avoid unnecessary stubbing
        unidadeDto = new UnidadeDto(10L, "UNIDADE TESTE", "UT", 1L, "DESCONHECIDO");
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocesso));
        when(sgrhService.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo("TITULAR123")).thenReturn(Optional.of(titularDto));

        eventListener.handleProcessoIniciado(evento);

        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se o responsável não for encontrado")
    void handleProcessoIniciado_ResponsavelNotFound() {
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocesso));
        when(sgrhService.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenReturn(Optional.empty());

        eventListener.handleProcessoIniciado(evento);

        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se o titular não tiver e-mail")
    void handleProcessoIniciado_TitularNoEmail() {
        titularDto = new UsuarioDto("TITULAR123", "Fulano", " ", "F123", "Analista");
        // Don't use full mock path as not all mocks are needed
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(subprocesso));
        when(sgrhService.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo("TITULAR123")).thenReturn(Optional.of(titularDto));

        eventListener.handleProcessoIniciado(evento);

        // Correct assertion: No email should be sent at all, as the code returns early.
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }
}