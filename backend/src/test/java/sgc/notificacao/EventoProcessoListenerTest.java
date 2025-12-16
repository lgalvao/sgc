package sgc.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import sgc.alerta.AlertaService;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoProcessoListenerTest {
    private static final String UNID_OP = "UNID-OP";
    private static final Long T123 = 123123123123L;
    private static final String TITULAR_TESTE = "Titular Teste";
    private static final String TITULAR_EMAIL = "titular@test.com";
    private static final String RAMAL = "12345";
    private static final Long S456 = 456456456456L;
    private static final String SUBSTITUTO_TESTE = "Substituto Teste";
    private static final String SUBSTITUTO_EMAIL = "substituto@test.com";
    private static final String RAMAL_SUBSTITUTO = "67890";

    @Mock
    private AlertaService alertaService;
    @Mock
    private NotificacaoEmailService notificacaoEmailService;
    @Mock
    private NotificacaoModelosService notificacaoModelosService;
    @Mock
    private SgrhService sgrhService;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private Environment environment;

    @InjectMocks
    private EventoProcessoListener ouvinteDeEvento;

    private Processo processo;
    private Subprocesso subprocessoOperacional;
    private EventoProcessoIniciado evento;

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
        subprocessoOperacional.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));

        evento = new EventoProcessoIniciado(1L, "INICIADO", LocalDateTime.now(), List.of(100L));
    }

    @Test
    @DisplayName("Deve processar evento, criar alertas e enviar e-mails para unidade operacional")
    void aoIniciarProcesso_deveProcessarCompleto_quandoUnidadeOperacional() {
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));

        UnidadeDto unidadeDto = UnidadeDto.builder()
                .codigo(100L)
                .nome("Unidade Operacional")
                .sigla(UNID_OP)
                .codigoPai(null)
                .tipo("OPERACIONAL")
                .isElegivel(false)
                .build();
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));

        ResponsavelDto responsavelDto = new ResponsavelDto(
                100L,
                String.valueOf(T123),
                TITULAR_TESTE,
                String.valueOf(S456),
                SUBSTITUTO_TESTE);
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));

        UsuarioDto titular = UsuarioDto.builder()
                .tituloEleitoral(String.valueOf(T123))
                .nome(TITULAR_TESTE)
                .email(TITULAR_EMAIL)
                .matricula(RAMAL)
                .build();
        UsuarioDto substituto = UsuarioDto.builder()
                .tituloEleitoral(String.valueOf(S456))
                .nome(SUBSTITUTO_TESTE)
                .email(SUBSTITUTO_EMAIL)
                .matricula(RAMAL_SUBSTITUTO)
                .build();

        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123)))
                .thenReturn(Optional.of(titular));

        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(S456)))
                .thenReturn(Optional.of(substituto));

        when(notificacaoModelosService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                .thenReturn("<html><body>Email Operacional</body></html>");

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, times(1))
                .criarAlertasProcessoIniciado(
                        processo,
                        List.of(subprocessoOperacional.getUnidade().getCodigo()),
                        List.of(subprocessoOperacional));

        verify(notificacaoEmailService, times(1))
                .enviarEmailHtml(eq(TITULAR_EMAIL), anyString(), anyString());
        verify(notificacaoEmailService, times(1))
                .enviarEmailHtml(eq(SUBSTITUTO_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve fazer nada se o processo não for encontrado")
    void aoIniciarProcesso_naoDeveFazerNada_quandoProcessoNaoEncontrado() {
        when(processoRepo.findById(1L)).thenReturn(Optional.empty());

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), anyList(), anyList());
        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mails se não houver subprocessos")
    void aoIniciarProcesso_naoDeveEnviarEmails_quandoNaoHouverSubprocessos() {
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(Collections.emptyList());

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, never()).criarAlertasProcessoIniciado(any(), anyList(), anyList());
        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve enviar e-mail correto para unidade INTERMEDIARIA")
    void aoIniciarProcesso_deveEnviarEmailCorreto_quandoUnidadeIntermediaria() {
        UnidadeDto unidadeDto =
                UnidadeDto.builder()
                        .codigo(100L)
                        .nome("Unidade Intermediaria")
                        .sigla("UNID-INT")
                        .codigoPai(null)
                        .tipo("INTERMEDIARIA")
                        .isElegivel(false)
                        .build();
        ResponsavelDto responsavelDto =
                new ResponsavelDto(
                        100L, String.valueOf(T123), TITULAR_TESTE, null, null); // Sem substituto
        UsuarioDto titular = UsuarioDto.builder()
                .tituloEleitoral(String.valueOf(T123))
                .nome(TITULAR_TESTE)
                .email(TITULAR_EMAIL)
                .matricula(RAMAL)
                .build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123)))
                .thenReturn(Optional.of(titular));
        when(notificacaoModelosService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                .thenReturn("<html><body>Email Intermediaria</body></html>");

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(alertaService, times(1)).criarAlertasProcessoIniciado(any(), anyList(), anyList());
        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoEmailService, times(1))
                .enviarEmailHtml(eq(TITULAR_EMAIL), assuntoCaptor.capture(), corpoCaptor.capture());

        assertEquals(
                "Processo Iniciado em Unidades Subordinadas - Teste de Processo",
                assuntoCaptor.getValue());
        verify(notificacaoEmailService, never())
                .enviarEmailHtml(eq(SUBSTITUTO_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve enviar e-mail correto para unidade INTEROPERACIONAL")
    void aoIniciarProcesso_deveEnviarEmailCorreto_quandoUnidadeInteroperacional() {
        UnidadeDto unidadeDto =
                UnidadeDto.builder()
                        .codigo(100L)
                        .nome("Unidade Interoperacional")
                        .sigla("UNID-IO")
                        .codigoPai(null)
                        .tipo("INTEROPERACIONAL")
                        .isElegivel(false)
                        .build();
        ResponsavelDto responsavelDto =
                new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null);
        UsuarioDto titular = UsuarioDto.builder()
                .tituloEleitoral(String.valueOf(T123))
                .nome(TITULAR_TESTE)
                .email(TITULAR_EMAIL)
                .matricula(RAMAL)
                .build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123)))
                .thenReturn(Optional.of(titular));
        when(notificacaoModelosService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                .thenReturn("<html><body>Email Interoperacional</body></html>");

        ouvinteDeEvento.aoIniciarProcesso(evento);

        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoEmailService)
                .enviarEmailHtml(eq(TITULAR_EMAIL), assuntoCaptor.capture(), anyString());
        assertEquals("Processo Iniciado - Teste de Processo", assuntoCaptor.getValue());
    }

    @Test
    @DisplayName("Não deve enviar e-mail para tipo de unidade desconhecido")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoTipoUnidadeDesconhecido() {
        UnidadeDto unidadeDto =
                UnidadeDto.builder()
                        .codigo(100L)
                        .nome("Unidade Desconhecida")
                        .sigla("UNID-DESC")
                        .codigoPai(null)
                        .tipo("DESCONHECIDO")
                        .isElegivel(false)
                        .build();
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se subprocesso não tiver unidade")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoSubprocessoSemUnidade() {
        subprocessoOperacional.setUnidade(null);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(sgrhService, never()).buscarUnidadePorCodigo(anyLong());
        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se responsável da unidade não for encontrado")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoResponsavelNaoEncontrado() {
        UnidadeDto unidadeDto =
                UnidadeDto.builder()
                        .codigo(100L)
                        .nome("Unidade Operacional")
                        .sigla("UNID-OP")
                        .codigoPai(null)
                        .tipo("OPERACIONAL")
                        .isElegivel(false)
                        .build();
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.empty());

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar e-mail se titular não tiver e-mail")
    void aoIniciarProcesso_naoDeveEnviarEmail_quandoTitularSemEmail() {
        UnidadeDto unidadeDto =
                UnidadeDto.builder()
                        .codigo(100L)
                        .nome("Unidade Operacional")
                        .sigla(UNID_OP)
                        .codigoPai(null)
                        .tipo("OPERACIONAL")
                        .isElegivel(false)
                        .build();
        ResponsavelDto responsavelDto =
                new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null);
        UsuarioDto titularSemEmail = UsuarioDto.builder()
                .tituloEleitoral(String.valueOf(T123))
                .nome(TITULAR_TESTE)
                .email(" ") // Email em branco
                .matricula(RAMAL)
                .build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123)))
                .thenReturn(Optional.of(titularSemEmail));

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Exception geral captura")
    void exceptionGeral() {
        when(processoRepo.findById(1L)).thenThrow(new RuntimeException("Erro DB"));
        ouvinteDeEvento.aoIniciarProcesso(evento);
        verify(subprocessoRepo, never()).findByProcessoCodigoWithUnidade(any());
    }

    @Test
    @DisplayName("Erro ao enviar email substituto")
    void erroEmailSubstituto() {
        UnidadeDto unidadeDto =
                UnidadeDto.builder()
                        .codigo(100L)
                        .nome("Unidade Operacional")
                        .sigla(UNID_OP)
                        .codigoPai(null)
                        .tipo("OPERACIONAL")
                        .isElegivel(false)
                        .build();
        ResponsavelDto responsavelDto =
                new ResponsavelDto(
                        100L,
                        String.valueOf(T123),
                        TITULAR_TESTE,
                        String.valueOf(S456),
                        SUBSTITUTO_TESTE);
        UsuarioDto titular = UsuarioDto.builder()
                .tituloEleitoral(String.valueOf(T123))
                .nome(TITULAR_TESTE)
                .email(TITULAR_EMAIL)
                .matricula(RAMAL)
                .build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(subprocessoOperacional));
        when(sgrhService.buscarUnidadePorCodigo(100L)).thenReturn(Optional.of(unidadeDto));
        when(sgrhService.buscarResponsavelUnidade(100L)).thenReturn(Optional.of(responsavelDto));
        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(T123)))
                .thenReturn(Optional.of(titular));

        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(S456)))
                .thenThrow(new RuntimeException("Erro SGRH"));

        ouvinteDeEvento.aoIniciarProcesso(evento);

        verify(notificacaoEmailService, times(1)).enviarEmailHtml(eq(TITULAR_EMAIL), any(), any());
        verify(notificacaoEmailService, never())
                .enviarEmailHtml(eq(SUBSTITUTO_EMAIL), any(), any());
    }

    @Test
    @DisplayName("Deve processar finalização e enviar e-mail para unidade operacional")
    void aoFinalizarProcesso_deveEnviarEmail_quandoUnidadeOperacional() {
        EventoProcessoFinalizado eventoFinalizado = new EventoProcessoFinalizado(1L, LocalDateTime.now());
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        unidade.setSigla(UNID_OP);
        unidade.setTipo(sgc.unidade.model.TipoUnidade.OPERACIONAL);
        processo.setParticipantes(java.util.Set.of(unidade));

        ResponsavelDto responsavelDto = new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null);
        UsuarioDto titular = UsuarioDto.builder()
                .tituloEleitoral(String.valueOf(T123))
                .nome(TITULAR_TESTE)
                .email(TITULAR_EMAIL)
                .matricula(RAMAL)
                .build();

        when(sgrhService.buscarResponsaveisUnidades(anyList())).thenReturn(java.util.Map.of(100L, responsavelDto));
        when(sgrhService.buscarUsuariosPorTitulos(anyList())).thenReturn(java.util.Map.of(String.valueOf(T123), titular));

        when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any()))
                .thenReturn("html-finalizacao");

        ouvinteDeEvento.aoFinalizarProcesso(eventoFinalizado);

        verify(notificacaoEmailService).enviarEmailHtml(eq(TITULAR_EMAIL), anyString(), eq("html-finalizacao"));
    }
}
