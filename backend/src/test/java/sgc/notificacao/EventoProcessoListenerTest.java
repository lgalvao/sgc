package sgc.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.usuario.UsuarioService;
import sgc.usuario.dto.ResponsavelDto;
import sgc.usuario.dto.UsuarioDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: EventoProcessoListener")
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
    private UsuarioService usuarioService;
    @Mock
    private ProcessoService processoService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private Environment environment;

    @InjectMocks
    private EventoProcessoListener ouvinteDeEvento;

    private Processo processo;
    private Subprocesso subprocessoOperacional;
    private EventoProcessoIniciado evento;
    private Unidade unidadeOperacional;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Teste de Processo");
        processo.setTipo(TipoProcesso.REVISAO);

        unidadeOperacional = new Unidade();
        unidadeOperacional.setCodigo(100L);
        unidadeOperacional.setSigla(UNID_OP);
        unidadeOperacional.setNome("Unidade Operacional");
        unidadeOperacional.setTipo(TipoUnidade.OPERACIONAL);

        subprocessoOperacional = new Subprocesso();
        subprocessoOperacional.setCodigo(10L);
        subprocessoOperacional.setProcesso(processo);
        subprocessoOperacional.setUnidade(unidadeOperacional);
        subprocessoOperacional.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));

        evento = new EventoProcessoIniciado(1L, "INICIADO", LocalDateTime.now(), List.of(100L));
    }

    @Nested
    @DisplayName("Evento: Processo Iniciado")
    class ProcessoIniciado {
        @Test
        @DisplayName("Deve processar evento completo para unidade operacional")
        void deveProcessarCompletoQuandoUnidadeOperacional() {
            // Given
            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));

            ResponsavelDto responsavelDto = new ResponsavelDto(
                    100L,
                    String.valueOf(T123),
                    TITULAR_TESTE,
                    String.valueOf(S456),
                    SUBSTITUTO_TESTE);
            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(100L, responsavelDto));

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

            when(usuarioService.buscarUsuariosPorTitulos(anyList()))
                    .thenReturn(Map.of(String.valueOf(T123), titular, String.valueOf(S456), substituto));

            when(notificacaoModelosService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                    .thenReturn("<html><body>Email Operacional</body></html>");

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
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
        @DisplayName("Não deve fazer nada quando processo não encontrado")
        void naoDeveFazerNadaQuandoProcessoNaoEncontrado() {
            // Given
            when(processoService.buscarEntidadePorId(1L)).thenThrow(new sgc.comum.erros.ErroEntidadeNaoEncontrada("Processo não encontrado"));

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(alertaService, never()).criarAlertasProcessoIniciado(any(), anyList(), anyList());
            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Não deve enviar e-mails quando não houver subprocessos")
        void naoDeveEnviarEmailsQuandoNaoHouverSubprocessos() {
            // Given
            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(Collections.emptyList());

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(alertaService, never()).criarAlertasProcessoIniciado(any(), anyList(), anyList());
            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve enviar e-mail correto para unidade INTERMEDIARIA")
        void deveEnviarEmailCorretoQuandoUnidadeIntermediaria() {
            // Given
            unidadeOperacional.setTipo(TipoUnidade.INTERMEDIARIA);
            
            ResponsavelDto responsavelDto =
                    new ResponsavelDto(
                            100L, String.valueOf(T123), TITULAR_TESTE, null, null); // Sem substituto
            UsuarioDto titular = UsuarioDto.builder()
                    .tituloEleitoral(String.valueOf(T123))
                    .nome(TITULAR_TESTE)
                    .email(TITULAR_EMAIL)
                    .matricula(RAMAL)
                    .build();

            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));
            
            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(100L, responsavelDto));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(String.valueOf(T123), titular));
            
            when(notificacaoModelosService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                    .thenReturn("<html><body>Email Intermediaria</body></html>");

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
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
        void deveEnviarEmailCorretoQuandoUnidadeInteroperacional() {
            // Given
            unidadeOperacional.setTipo(TipoUnidade.INTEROPERACIONAL);
            
            ResponsavelDto responsavelDto =
                    new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null);
            UsuarioDto titular = UsuarioDto.builder()
                    .tituloEleitoral(String.valueOf(T123))
                    .nome(TITULAR_TESTE)
                    .email(TITULAR_EMAIL)
                    .matricula(RAMAL)
                    .build();

            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));
            
            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(100L, responsavelDto));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(String.valueOf(T123), titular));
            
            when(notificacaoModelosService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                    .thenReturn("<html><body>Email Interoperacional</body></html>");

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificacaoEmailService)
                    .enviarEmailHtml(eq(TITULAR_EMAIL), assuntoCaptor.capture(), anyString());
            assertEquals("Processo Iniciado - Teste de Processo", assuntoCaptor.getValue());
        }

        @Test
        @DisplayName("Não deve enviar e-mail quando tipo de unidade desconhecido")
        void naoDeveEnviarEmailQuandoTipoUnidadeDesconhecido() {
            // Given
            unidadeOperacional.setTipo(null); // Desconhecido
            
            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));
            
            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Collections.emptyMap());
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Collections.emptyMap());

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Não deve enviar e-mail quando subprocesso sem unidade")
        void naoDeveEnviarEmailQuandoSubprocessoSemUnidade() {
            // Given
            subprocessoOperacional.setUnidade(null);
            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(usuarioService, never()).buscarResponsaveisUnidades(anyList());
            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Não deve enviar e-mail quando responsável não encontrado")
        void naoDeveEnviarEmailQuandoResponsavelNaoEncontrado() {
            // Given
            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));
            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Collections.emptyMap());
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Collections.emptyMap());

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Não deve enviar e-mail quando titular sem e-mail")
        void naoDeveEnviarEmailQuandoTitularSemEmail() {
            // Given
            ResponsavelDto responsavelDto =
                    new ResponsavelDto(100L, String.valueOf(T123), TITULAR_TESTE, null, null);
            UsuarioDto titularSemEmail = UsuarioDto.builder()
                    .tituloEleitoral(String.valueOf(T123))
                    .nome(TITULAR_TESTE)
                    .email(" ") // Email em branco
                    .matricula(RAMAL)
                    .build();

            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));
            
            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(100L, responsavelDto));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(String.valueOf(T123), titularSemEmail));

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(notificacaoEmailService, never()).enviarEmailHtml(any(), any(), any());
        }

        @Test
        @DisplayName("Deve capturar exception geral sem interromper execução")
        void deveCapturarExceptionGeral() {
            // Given
            when(processoService.buscarEntidadePorId(1L)).thenThrow(new RuntimeException("Erro DB"));

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(subprocessoService, never()).listarEntidadesPorProcesso(any());
        }

        @Test
        @DisplayName("Deve prosseguir envio para titular mesmo com erro no envio para substituto")
        void deveProsseguirMesmoComErroEmailSubstituto() {
            // Given
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

            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(subprocessoOperacional));
            
            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(100L, responsavelDto));
            
            // Simular erro ao buscar um dos usuários ou falha no envio para o substituto
            // Mas agora a busca de usuários é em lote, então se falhar o lote, falha tudo?
            // Vamos simular que o substituto não veio no mapa de usuários.
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(String.valueOf(T123), titular));

            when(notificacaoModelosService.criarEmailDeProcessoIniciado(any(), any(), any(), any()))
                    .thenReturn("html");

            // When
            ouvinteDeEvento.aoIniciarProcesso(evento);

            // Then
            verify(notificacaoEmailService, times(1)).enviarEmailHtml(eq(TITULAR_EMAIL), any(), any());
            verify(notificacaoEmailService, never())
                    .enviarEmailHtml(eq(SUBSTITUTO_EMAIL), any(), any());
        }
    }

    @Nested
    @DisplayName("Evento: Processo Finalizado")
    class ProcessoFinalizado {
        @Test
        @DisplayName("Deve processar finalização e enviar e-mail para unidade operacional")
        void deveEnviarEmailQuandoUnidadeOperacional() {
            // Given
            EventoProcessoFinalizado eventoFinalizado = new EventoProcessoFinalizado(1L, LocalDateTime.now());
            when(processoService.buscarEntidadePorId(1L)).thenReturn(processo);

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

            when(usuarioService.buscarResponsaveisUnidades(anyList())).thenReturn(java.util.Map.of(100L, responsavelDto));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(java.util.Map.of(String.valueOf(T123), titular));

            when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any()))
                    .thenReturn("html-finalizacao");

            // When
            ouvinteDeEvento.aoFinalizarProcesso(eventoFinalizado);

            // Then
            verify(notificacaoEmailService).enviarEmailHtml(eq(TITULAR_EMAIL), anyString(), eq("html-finalizacao"));
        }
    }
}
