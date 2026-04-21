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
