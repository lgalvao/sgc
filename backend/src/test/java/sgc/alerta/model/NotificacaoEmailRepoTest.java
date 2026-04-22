package sgc.alerta.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("NotificacaoEmailRepo - Testes de Repositório")
@SuppressWarnings("NullAway.Init")
class NotificacaoEmailRepoTest {
    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @MockitoBean
    private EmailService emailService;

    @Test
    @DisplayName("deve salvar e recuperar por chave idempotente")
    void deveSalvarERecuperarPorChaveIdempotente() {
        NotificacaoEmail salva = notificacaoEmailRepo.save(criarNotificacao("chave-repo-1"));

        assertThat(notificacaoEmailRepo.findByChaveIdempotencia("chave-repo-1")).get().satisfies(notificacao -> {
            assertThat(notificacao.getCodigo()).isEqualTo(salva.getCodigo());
            assertThat(notificacao.getSubprocesso()).isNotNull();
            assertThat(notificacao.getSubprocesso().getCodigo()).isEqualTo(60000L);
            assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacaoEmail.PENDENTE);
        });
    }

    @Test
    @DisplayName("deve listar notificacoes pendentes vencidas")
    void deveListarNotificacoesPendentesVencidas() {
        LocalDateTime agora = LocalDateTime.of(2026, 4, 21, 9, 0);
        notificacaoEmailRepo.save(criarNotificacao("chave-repo-vencida"));
        NotificacaoEmail futura = criarNotificacao("chave-repo-futura");
        futura.setProximaTentativaEm(agora.plusMinutes(1));
        notificacaoEmailRepo.save(futura);

        List<NotificacaoEmail> pendentes = notificacaoEmailRepo
                .findBySituacaoInAndProximaTentativaEmLessThanEqualOrderByDataHoraCriacaoAsc(
                        List.of(SituacaoNotificacaoEmail.PENDENTE, SituacaoNotificacaoEmail.FALHA_TEMPORARIA),
                        agora,
                        PageRequest.of(0, 10)
                );

        assertThat(pendentes)
                .extracting(NotificacaoEmail::getChaveIdempotencia)
                .contains("chave-repo-vencida")
                .doesNotContain("chave-repo-futura");
    }

    @Test
    @DisplayName("deve listar notificacoes por subprocesso")
    void deveListarNotificacoesPorSubprocesso() {
        notificacaoEmailRepo.save(criarNotificacao("chave-repo-subprocesso"));

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(
                60000L,
                PageRequest.of(0, 10)
        );

        assertThat(notificacoes)
                .extracting(NotificacaoEmail::getChaveIdempotencia)
                .contains("chave-repo-subprocesso");
    }

    @Test
    @DisplayName("deve capturar notificacao disponivel de forma atomica")
    void deveCapturarNotificacaoDisponivelDeFormaAtomica() {
        NotificacaoEmail notificacao = notificacaoEmailRepo.save(criarNotificacao("chave-repo-claim"));

        int capturadas = notificacaoEmailRepo.marcarEnviandoSeDisponivel(
                notificacao.getCodigo(),
                LocalDateTime.of(2026, 4, 21, 9, 0)
        );

        assertThat(capturadas).isOne();
        assertThat(notificacaoEmailRepo.findById(notificacao.getCodigo())).get()
                .extracting(NotificacaoEmail::getSituacao)
                .isEqualTo(SituacaoNotificacaoEmail.ENVIANDO);
    }

    @Test
    @DisplayName("nao deve capturar notificacao futura ou ja capturada")
    void naoDeveCapturarNotificacaoFuturaOuJaCapturada() {
        NotificacaoEmail futura = criarNotificacao("chave-repo-futura-claim");
        futura.setProximaTentativaEm(LocalDateTime.of(2026, 4, 21, 10, 0));
        notificacaoEmailRepo.save(futura);
        NotificacaoEmail enviando = criarNotificacao("chave-repo-enviando-claim");
        enviando.setSituacao(SituacaoNotificacaoEmail.ENVIANDO);
        notificacaoEmailRepo.save(enviando);

        int futurasCapturadas = notificacaoEmailRepo.marcarEnviandoSeDisponivel(
                futura.getCodigo(),
                LocalDateTime.of(2026, 4, 21, 9, 0)
        );
        int enviandoCapturadas = notificacaoEmailRepo.marcarEnviandoSeDisponivel(
                enviando.getCodigo(),
                LocalDateTime.of(2026, 4, 21, 9, 0)
        );

        assertThat(futurasCapturadas).isZero();
        assertThat(enviandoCapturadas).isZero();
    }

    @Test
    @DisplayName("deve resumir notificacoes por subprocessos de processos ativos")
    void deveResumirNotificacoesPorSubprocessosDeProcessosAtivos() {
        NotificacaoEmail falha = criarNotificacao("chave-repo-resumo-falha");
        falha.setSituacao(SituacaoNotificacaoEmail.FALHA_DEFINITIVA);
        falha.setTentativas(5);
        falha.setUltimoErro("SMTP fora");
        notificacaoEmailRepo.save(falha);

        List<sgc.alerta.dto.NotificacaoSubprocessoResumoQuery> resumos =
                notificacaoEmailRepo.resumirPorSubprocessosDeProcessosAtivos();

        assertThat(resumos)
                .filteredOn(resumo -> Objects.equals(resumo.subprocessoCodigo(), 60000L))
                .first()
                .satisfies(resumo -> {
                    assertThat(resumo.totalNotificacoes()).isGreaterThanOrEqualTo(1);
                    assertThat(resumo.falhasDefinitivas()).isGreaterThanOrEqualTo(1);
                    assertThat(resumo.maiorTentativas()).isGreaterThanOrEqualTo(5);
                });
    }

    @Test
    @DisplayName("deve reenfileirar falhas definitivas por subprocesso")
    void deveReenfileirarFalhasDefinitivasPorSubprocesso() {
        NotificacaoEmail falha = criarNotificacao("chave-repo-reenviar");
        falha.setSituacao(SituacaoNotificacaoEmail.FALHA_DEFINITIVA);
        falha.setTentativas(5);
        falha.setProximaTentativaEm(null);
        falha.setUltimoErro("SMTP fora");
        notificacaoEmailRepo.save(falha);

        int reenfileiradas = notificacaoEmailRepo.reenfileirarFalhasDefinitivasPorSubprocesso(
                60000L,
                LocalDateTime.of(2026, 4, 21, 9, 0)
        );

        assertThat(reenfileiradas).isOne();
        assertThat(notificacaoEmailRepo.findByChaveIdempotencia("chave-repo-reenviar")).get()
                .satisfies(notificacao -> {
                    assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacaoEmail.PENDENTE);
                    assertThat(notificacao.getTentativas()).isZero();
                    assertThat(notificacao.getProximaTentativaEm()).isEqualTo(LocalDateTime.of(2026, 4, 21, 9, 0));
                    assertThat(notificacao.getUltimoErro()).isNull();
                });
    }

    private NotificacaoEmail criarNotificacao(String chaveIdempotencia) {
        return NotificacaoEmail.builder()
                .subprocesso(subprocessoRepo.findById(60000L).orElseThrow())
                .tipoNotificacao("PROCESSO_INICIADO")
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .situacao(SituacaoNotificacaoEmail.PENDENTE)
                .tentativas(0)
                .proximaTentativaEm(LocalDateTime.of(2026, 4, 21, 8, 0))
                .dataHoraCriacao(LocalDateTime.of(2026, 4, 21, 8, 0))
                .chaveIdempotencia(chaveIdempotencia)
                .build();
    }
}
