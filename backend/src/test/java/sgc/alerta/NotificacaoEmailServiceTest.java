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
@DisplayName("NotificacaoEmailService")
@SuppressWarnings("NullAway.Init")
class NotificacaoEmailServiceTest {
    private static final Instant INSTANTE_FIXO = Instant.parse("2026-04-21T12:00:00Z");
    private static final ZoneId ZONA = ZoneId.of("America/Recife");

    @Mock
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Captor
    private ArgumentCaptor<NotificacaoEmail> notificacaoCaptor;

    private NotificacaoEmailService service;

    @BeforeEach
    void setUp() {
        service = new NotificacaoEmailService(notificacaoEmailRepo, Clock.fixed(INSTANTE_FIXO, ZONA));
    }

    @Test
    @DisplayName("enfileirar deve criar notificacao pendente")
    void enfileirarDeveCriarNotificacaoPendente() {
        when(notificacaoEmailRepo.existsByChaveIdempotencia("chave-1")).thenReturn(false);
        when(notificacaoEmailRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.enfileirar(new EnfileirarNotificacaoEmailCommand(
                null,
                "PROCESSO_INICIADO",
                "destino@tre-pe.jus.br",
                "Assunto",
                "<p>corpo</p>",
                "chave-1"
        ));

        verify(notificacaoEmailRepo).save(notificacaoCaptor.capture());
        assertThat(notificacaoCaptor.getValue()).satisfies(notificacao -> {
            assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacaoEmail.PENDENTE);
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

        NotificacaoEmail resultado = service.enfileirar(new EnfileirarNotificacaoEmailCommand(
                null,
                "PROCESSO_INICIADO",
                "destino@tre-pe.jus.br",
                "Assunto",
                "<p>corpo</p>",
                "chave-1"
        ));

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

        NotificacaoEmail resultado = service.enfileirar(new EnfileirarNotificacaoEmailCommand(
                null,
                "PROCESSO_INICIADO",
                "destino@tre-pe.jus.br",
                "Assunto",
                "<p>corpo</p>",
                "chave-1"
        ));

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
                eq(List.of(SituacaoNotificacaoEmail.PENDENTE, SituacaoNotificacaoEmail.FALHA_TEMPORARIA)),
                eq(LocalDateTime.of(2026, 4, 21, 9, 0)),
                any()
        );
    }

    @Test
    @DisplayName("marcarFalha deve agendar retry antes do limite")
    void marcarFalhaDeveAgendarRetryAntesDoLimite() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .tentativas(0)
                .build();

        service.marcarFalha(notificacao, new RuntimeException("SMTP indisponivel"));

        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacaoEmail.FALHA_TEMPORARIA);
        assertThat(notificacao.getTentativas()).isEqualTo(1);
        assertThat(notificacao.getUltimoErro()).isEqualTo("SMTP indisponivel");
        assertThat(notificacao.getProximaTentativaEm()).isEqualTo(LocalDateTime.of(2026, 4, 21, 9, 0, 20));
        verify(notificacaoEmailRepo).save(notificacao);
    }

    @Test
    @DisplayName("marcarFalha deve encerrar apos limite de tentativas")
    void marcarFalhaDeveEncerrarAposLimiteDeTentativas() {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .tentativas(4)
                .build();

        service.marcarFalha(notificacao, new RuntimeException("erro definitivo"));

        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacaoEmail.FALHA_DEFINITIVA);
        assertThat(notificacao.getTentativas()).isEqualTo(5);
        assertThat(notificacao.getProximaTentativaEm()).isNull();
        verify(notificacaoEmailRepo).save(notificacao);
    }
}
