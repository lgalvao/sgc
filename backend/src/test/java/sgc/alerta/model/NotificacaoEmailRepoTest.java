package sgc.alerta.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.EmailService;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
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
                        List.of(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.FALHA_TEMPORARIA),
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
    @DisplayName("deve listar todas as notificacoes para administracao inclusive sem subprocesso e de processo finalizado")
    void deveListarTodasAsNotificacoesParaAdministracaoInclusiveSemSubprocessoEDeProcessoFinalizado() {
        NotificacaoEmail semSubprocesso = criarNotificacao("chave-repo-sem-subprocesso");
        semSubprocesso.setSubprocesso(null);
        semSubprocesso.setDataHoraCriacao(LocalDateTime.of(2026, 4, 21, 10, 0));
        notificacaoEmailRepo.save(semSubprocesso);

        Subprocesso subprocessoFinalizado = subprocessoRepo.findById(60000L).orElseThrow();
        Processo processoFinalizado = subprocessoFinalizado.getProcesso();
        processoFinalizado.setSituacao(SituacaoProcesso.FINALIZADO);

        NotificacaoEmail deProcessoFinalizado = criarNotificacao("chave-repo-processo-finalizado");
        deProcessoFinalizado.setDataHoraCriacao(LocalDateTime.of(2026, 4, 21, 9, 0));
        notificacaoEmailRepo.save(deProcessoFinalizado);

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAllByOrderByDataHoraCriacaoDesc(
                PageRequest.of(0, 10)
        );

        assertThat(notificacoes)
                .extracting(NotificacaoEmail::getChaveIdempotencia)
                .contains("chave-repo-sem-subprocesso", "chave-repo-processo-finalizado");
        assertThat(notificacoes.getFirst().getChaveIdempotencia()).isEqualTo("chave-repo-sem-subprocesso");
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
                .isEqualTo(SituacaoNotificacao.ENVIANDO);
    }

    @Test
    @DisplayName("nao deve capturar notificacao futura ou ja capturada")
    void naoDeveCapturarNotificacaoFuturaOuJaCapturada() {
        NotificacaoEmail futura = criarNotificacao("chave-repo-futura-claim");
        futura.setProximaTentativaEm(LocalDateTime.of(2026, 4, 21, 10, 0));
        notificacaoEmailRepo.save(futura);
        NotificacaoEmail enviando = criarNotificacao("chave-repo-enviando-claim");
        enviando.setSituacao(SituacaoNotificacao.ENVIANDO);
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
        falha.setSituacao(SituacaoNotificacao.FALHA_DEFINITIVA);
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
    @DisplayName("nao deve listar subprocessos ativos sem notificacao")
    void naoDeveListarSubprocessosAtivosSemNotificacao() {
        List<sgc.alerta.dto.NotificacaoSubprocessoResumoQuery> resumos =
                notificacaoEmailRepo.resumirPorSubprocessosDeProcessosAtivos();

        assertThat(resumos)
                .extracting(sgc.alerta.dto.NotificacaoSubprocessoResumoQuery::subprocessoCodigo)
                .doesNotContain(1700L);
    }

    @Test
    @DisplayName("deve reenfileirar falhas definitivas por subprocesso")
    void deveReenfileirarFalhasDefinitivasPorSubprocesso() {
        NotificacaoEmail falha = criarNotificacao("chave-repo-reenviar");
        falha.setSituacao(SituacaoNotificacao.FALHA_DEFINITIVA);
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
                    assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
                    assertThat(notificacao.getTentativas()).isZero();
                    assertThat(notificacao.getProximaTentativaEm()).isEqualTo(LocalDateTime.of(2026, 4, 21, 9, 0));
                    assertThat(notificacao.getUltimoErro()).isNull();
                });
    }

    private NotificacaoEmail criarNotificacao(String chaveIdempotencia) {
        return NotificacaoEmail.builder()
                .subprocesso(subprocessoRepo.findById(60000L).orElseThrow())
                .tipoNotificacao(TipoNotificacao.PROCESSO_INICIADO)
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .situacao(SituacaoNotificacao.PENDENTE)
                .tentativas(0)
                .proximaTentativaEm(LocalDateTime.of(2026, 4, 21, 8, 0))
                .dataHoraCriacao(LocalDateTime.of(2026, 4, 21, 8, 0))
                .chaveIdempotencia(chaveIdempotencia)
                .build();
    }
}
