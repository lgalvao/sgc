package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.dao.*;
import sgc.alerta.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoService")
@SuppressWarnings("NullAway.Init")
class NotificacaoServiceTest {
    private static final Instant INSTANTE_FIXO = Instant.parse("2026-04-21T12:00:00Z");
    private static final ZoneId ZONA = ZoneId.of("America/Recife");

    @Mock
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Captor
    private ArgumentCaptor<NotificacaoEmail> notificacaoCaptor;

    private final AlertaDtoMapper alertaDtoMapper = new AlertaDtoMapper();
    private NotificacaoService service;

    @BeforeEach
    void setUp() {
        service = new NotificacaoService(notificacaoEmailRepo, Clock.fixed(INSTANTE_FIXO, ZONA), alertaDtoMapper);
    }

    @Test
    @DisplayName("enfileirar deve criar notificacao pendente")
    void enfileirarDeveCriarNotificacaoPendente() {
        when(notificacaoEmailRepo.existsByChaveIdempotencia("chave-1")).thenReturn(false);
        when(notificacaoEmailRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.enfileirar(EnfileirarNotificacaoCommand.builder()
                .tipoNotificacao(TipoNotificacao.PROCESSO_INICIADO)
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .chaveIdempotencia("chave-1")
                .build());

        verify(notificacaoEmailRepo).save(notificacaoCaptor.capture());
        assertThat(notificacaoCaptor.getValue()).satisfies(notificacao -> {
            assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
            assertThat(notificacao.getTentativas()).isZero();
            assertThat(notificacao.getProximaTentativaEm()).isEqualTo(LocalDateTime.of(2026, 4, 21, 9, 0));
            assertThat(notificacao.getDataHoraCriacao()).isEqualTo(LocalDateTime.of(2026, 4, 21, 9, 0));
            assertThat(notificacao.getChaveIdempotencia()).isEqualTo("chave-1");
        });
    }

    @Test
    @DisplayName("enfileirar deve retornar notificacao existente pela chave idempotente")
    void enfileirarDeveRetornarNotificacaoExistente() {
        NotificacaoEmail existente = NotificacaoEmail.builder()
                .chaveIdempotencia("chave-1")
                .build();

        when(notificacaoEmailRepo.existsByChaveIdempotencia("chave-1")).thenReturn(true);
        when(notificacaoEmailRepo.findByChaveIdempotencia("chave-1")).thenReturn(Optional.of(existente));

        NotificacaoEmail resultado = service.enfileirar(EnfileirarNotificacaoCommand.builder()
                .tipoNotificacao(TipoNotificacao.PROCESSO_INICIADO)
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .chaveIdempotencia("chave-1")
                .build());

        assertThat(resultado).isSameAs(existente);
        verify(notificacaoEmailRepo, never()).save(any());
    }

    @Test
    @DisplayName("enfileirar deve tolerar corrida de chave idempotente")
    void enfileirarDeveTolerarCorridaDeChaveIdempotente() {
        NotificacaoEmail existente = NotificacaoEmail.builder()
                .chaveIdempotencia("chave-1")
                .build();

        when(notificacaoEmailRepo.existsByChaveIdempotencia("chave-1")).thenReturn(false);
        when(notificacaoEmailRepo.save(any())).thenThrow(new DataIntegrityViolationException("duplicado"));
        when(notificacaoEmailRepo.findByChaveIdempotencia("chave-1")).thenReturn(Optional.of(existente));

        NotificacaoEmail resultado = service.enfileirar(EnfileirarNotificacaoCommand.builder()
                .tipoNotificacao(TipoNotificacao.PROCESSO_INICIADO)
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .chaveIdempotencia("chave-1")
                .build());

        assertThat(resultado).isSameAs(existente);
    }

    @Test
    @DisplayName("listarPendentes deve buscar pendentes e falhas temporarias vencidas")
    void listarPendentesDeveBuscarPendentesEFalhasTemporariasVencidas() {
        when(notificacaoEmailRepo.findBySituacaoInAndProximaTentativaEmLessThanEqualOrderByDataHoraCriacaoAsc(
                anyCollection(),
                any(),
                any()
        )).thenReturn(List.of());

        service.listarPendentes(10);

        verify(notificacaoEmailRepo).findBySituacaoInAndProximaTentativaEmLessThanEqualOrderByDataHoraCriacaoAsc(
                eq(List.of(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.FALHA_TEMPORARIA)),
                eq(LocalDateTime.of(2026, 4, 21, 9, 0)),
                any()
        );
    }

    @Test
    @DisplayName("listarPorSubprocesso deve limitar tamanho da consulta")
    void listarPorSubprocessoDeveLimitarTamanhoDaConsulta() {
        when(notificacaoEmailRepo.findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(eq(60000L), any()))
                .thenReturn(List.of());

        service.listarPorSubprocesso(60000L, 500);

        verify(notificacaoEmailRepo).findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(eq(60000L), argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 100
        ));
    }

    @Test
    @DisplayName("marcarEnviandoSeDisponivel deve capturar notificacao pendente")
    void marcarEnviandoSeDisponivelDeveCapturarNotificacaoPendente() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(10L)
                .situacao(SituacaoNotificacao.PENDENTE)
                .build();
        when(notificacaoEmailRepo.marcarEnviandoSeDisponivel(10L, LocalDateTime.of(2026, 4, 21, 9, 0)))
                .thenReturn(1);

        boolean capturada = service.marcarEnviandoSeDisponivel(notificacao);

        assertThat(capturada).isTrue();
        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.ENVIANDO);
    }

    @Test
    @DisplayName("marcarEnviandoSeDisponivel deve rejeitar notificacao ja capturada")
    void marcarEnviandoSeDisponivelDeveRejeitarNotificacaoJaCapturada() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(10L)
                .situacao(SituacaoNotificacao.PENDENTE)
                .build();
        when(notificacaoEmailRepo.marcarEnviandoSeDisponivel(10L, LocalDateTime.of(2026, 4, 21, 9, 0)))
                .thenReturn(0);

        boolean capturada = service.marcarEnviandoSeDisponivel(notificacao);

        assertThat(capturada).isFalse();
        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
    }

    @Test
    @DisplayName("reenfileirarFalhasDefinitivasPorSubprocesso deve delegar com data atual")
    void reenfileirarFalhasDefinitivasPorSubprocessoDeveDelegarComDataAtual() {
        when(notificacaoEmailRepo.reenfileirarFalhasDefinitivasPorSubprocesso(
                60000L,
                LocalDateTime.of(2026, 4, 21, 9, 0)
        )).thenReturn(2);

        int reenfileiradas = service.reenfileirarFalhasDefinitivasPorSubprocesso(60000L);

        assertThat(reenfileiradas).isEqualTo(2);
    }

    @Test
    @DisplayName("listarTodasAdmin deve buscar todas as notificações registradas")
    void listarTodasAdminDeveBuscarTodasAsNotificacoesRegistradas() {
        when(notificacaoEmailRepo.listarTodasOrdenadas(any())).thenReturn(List.of());

        service.listarTodasAdmin(50);

        verify(notificacaoEmailRepo).listarTodasOrdenadas(argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 50
        ));
    }

    @Test
    @DisplayName("reenviarPorCodigo deve delegar com data atual")
    void reenviarPorCodigoDeveDelegarComDataAtual() {
        when(notificacaoEmailRepo.reenviarPorCodigo(
                123L,
                LocalDateTime.of(2026, 4, 21, 9, 0)
        )).thenReturn(1);

        int reenfileiradas = service.reenviarPorCodigo(123L);

        assertThat(reenfileiradas).isEqualTo(1);
    }

    @Test
    @DisplayName("marcarFalha deve agendar retry antes do limite")
    void marcarFalhaDeveAgendarRetryAntesDoLimite() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(10L)
                .tentativas(0)
                .build();

        service.marcarFalha(notificacao, new RuntimeException("SMTP indisponivel"));

        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.FALHA_TEMPORARIA);
        assertThat(notificacao.getTentativas()).isEqualTo(1);
        assertThat(notificacao.getUltimoErro()).isEqualTo("SMTP indisponivel");
        assertThat(notificacao.getProximaTentativaEm()).isEqualTo(LocalDateTime.of(2026, 4, 21, 9, 0, 20));
        verify(notificacaoEmailRepo).marcarFalha(argThat(cmd ->
                cmd.codigo().equals(10L)
                        && cmd.situacao() == SituacaoNotificacao.FALHA_TEMPORARIA
                        && cmd.tentativas() == 1
                        && cmd.ultimoErro().equals("SMTP indisponivel")
                        && cmd.proximaTentativaEm().equals(LocalDateTime.of(2026, 4, 21, 9, 0, 20))
        ));
    }

    @Test
    @DisplayName("marcarFalha deve encerrar apos limite de tentativas")
    void marcarFalhaDeveEncerrarAposLimiteDeTentativas() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(10L)
                .tentativas(4)
                .build();

        service.marcarFalha(notificacao, new RuntimeException("erro definitivo"));

        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.FALHA_DEFINITIVA);
        assertThat(notificacao.getTentativas()).isEqualTo(5);
        assertThat(notificacao.getProximaTentativaEm()).isNull();
        verify(notificacaoEmailRepo).marcarFalha(argThat(cmd ->
                cmd.codigo().equals(10L)
                        && cmd.situacao() == SituacaoNotificacao.FALHA_DEFINITIVA
                        && cmd.tentativas() == 5
                        && cmd.ultimoErro().equals("erro definitivo")
                        && cmd.proximaTentativaEm() == null
        ));
    }

    @Test
    @DisplayName("marcarFalha deve usar nome da classe quando mensagem de erro for nula")
    void marcarFalhaDeveUsarNomeDaClasseQuandoMensagemDeErroForNula() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(20L)
                .tentativas(0)
                .build();

        service.marcarFalha(notificacao, new RuntimeException((String) null));

        assertThat(notificacao.getUltimoErro()).isEqualTo(RuntimeException.class.getName());
    }

    @Test
    @DisplayName("marcarFalha deve usar nome da classe quando mensagem de erro for vazia")
    void marcarFalhaDeveUsarNomeDaClasseQuandoMensagemDeErroForVazia() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(20L)
                .tentativas(0)
                .build();

        service.marcarFalha(notificacao, new RuntimeException(""));

        assertThat(notificacao.getUltimoErro()).isEqualTo(RuntimeException.class.getName());
    }

    @Test
    @DisplayName("marcarFalha deve truncar mensagem de erro acima do limite")
    void marcarFalhaDeveTruncarMensagemDeErroAcimaDoLimite() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(20L)
                .tentativas(0)
                .build();
        String mensagemLonga = "x".repeat(3000);

        service.marcarFalha(notificacao, new RuntimeException(mensagemLonga));

        assertThat(notificacao.getUltimoErro()).hasSize(2000);
    }

    @Test
    @DisplayName("listarResumoSubprocessosAtivos deve mapear consultas do repositório")
    void listarResumoSubprocessosAtivosDeveMapearConsultasDoRepositorio() {
        LocalDateTime ultimaNotificacao = LocalDateTime.of(2026, 4, 20, 10, 0);
        when(notificacaoEmailRepo.resumirPorSubprocessosDeProcessosAtivos()).thenReturn(List.of(
                new sgc.alerta.dto.NotificacaoSubprocessoResumoQuery(
                        10L,
                        20L,
                        "Processo ativo",
                        "U1",
                        sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        3,
                        1,
                        0,
                        2,
                        0,
                        0,
                        ultimaNotificacao,
                        ultimaNotificacao.plusDays(1),
                        2,
                        null
                )
        ));

        var resultado = service.listarResumoSubprocessosAtivos();

        assertThat(resultado).singleElement().satisfies(resumo -> {
            assertThat(resumo.subprocessoCodigo()).isEqualTo(10L);
            assertThat(resumo.processoDescricao()).isEqualTo("Processo ativo");
            assertThat(resumo.statusGeral()).isEqualTo("PENDENTE");
            assertThat(resumo.podeReenviar()).isFalse();
        });
    }
}
