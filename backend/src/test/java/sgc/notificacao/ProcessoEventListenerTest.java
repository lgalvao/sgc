package sgc.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private Processo processo;
    private Subprocesso subprocessoOperacional;
    private Unidade unidadeOperacional;
    private EventoProcessoIniciado evento;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Teste de Processo");
        processo.setTipo("REVISAO");

        unidadeOperacional = new Unidade();
        unidadeOperacional.setCodigo(100L);
        unidadeOperacional.setSigla("UNID-OP");

        subprocessoOperacional = new Subprocesso();
        subprocessoOperacional.setCodigo(10L);
        subprocessoOperacional.setProcesso(processo);
        subprocessoOperacional.setUnidade(unidadeOperacional);
        subprocessoOperacional.setDataLimiteEtapa1(LocalDate.now().plusDays(10));

        evento = new EventoProcessoIniciado(1L, "INICIADO", LocalDateTime.now(), List.of(100L));
    }

    @Test
    @DisplayName("Deve processar evento, criar alertas e enviar e-mails para unidade operacional")
    void handleProcessoIniciado_deveProcessarCompleto_quandoUnidadeOperacional() {
        // Arrange
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));

        UnidadeDto unidadeDto = new UnidadeDto(100L, "Unidade Operacional", "UNID-OP", null, "OPERACIONAL");
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));

        ResponsavelDto responsavelDto = new ResponsavelDto(100L, "T123", "Titular Teste", "S456", "Substituto Teste");
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));

        UsuarioDto titular = new UsuarioDto("T123", "Titular Teste", "titular@test.com", "12345", "Analista");
        UsuarioDto substituto = new UsuarioDto("S456", "Substituto Teste", "substituto@test.com", "67890", "Tecnico");
        when(sgrhService.buscarUsuarioPorTitulo("T123")).thenReturn(Optional.of(titular));
        when(sgrhService.buscarUsuarioPorTitulo("S456")).thenReturn(Optional.of(substituto));

        when(emailTemplateService.criarEmailProcessoIniciado(any(), any(), any(), any()))
                .thenReturn("<html><body>Email Operacional</body></html>");

        // Act
        eventListener.handleProcessoIniciado(evento);

        // Assert
        verify(alertaService, times(1)).criarAlertasProcessoIniciado(processo, List.of(subprocessoOperacional));
        
        verify(emailService, times(1)).enviarEmailHtml(
                eq("titular@test.com"),
                anyString(),
                contains("Email Operacional")
        );
        verify(emailService, times(1)).enviarEmailHtml(
                eq("substituto@test.com"),
                anyString(),
                contains("Email Operacional")
        );
    }
    
    @Test
    @DisplayName("Não deve fazer nada se o processo não for encontrado")
    void handleProcessoIniciado_naoDeveFazerNada_quandoProcessoNaoEncontrado() {
        // Arrange
        when(processoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        eventListener.handleProcessoIniciado(evento);

        // Assert
        verify(subprocessoRepository, never()).findByProcessoCodigoWithUnidade(anyLong());
        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), any());
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mails se não houver subprocessos")
    void handleProcessoIniciado_naoDeveEnviarEmails_quandoNaoHouverSubprocessos() {
        // Arrange
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());

        // Act
        eventListener.handleProcessoIniciado(evento);

        // Assert
        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), any());
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }
}