package sgc.alerta.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("NotificacaoRepo - Testes de Repositório")
class NotificacaoRepoTest {

    @Autowired
    private NotificacaoRepo notificacaoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Test
    @DisplayName("deve salvar e recuperar notificacao")
    void deveSalvarERecuperarNotificacao() {
        Notificacao notificacao = Notificacao.builder()
                .subprocesso(subprocessoRepo.findById(60000L).orElseThrow())
                .unidadeOrigem(unidadeRepo.findById(1L).orElseThrow())
                .unidadeDestino(unidadeRepo.findById(8L).orElseThrow())
                .dataHora(LocalDateTime.of(2025, 1, 3, 12, 0))
                .conteudo("Notificacao de teste")
                .build();

        Notificacao salva = notificacaoRepo.save(notificacao);

        assertThat(notificacaoRepo.findById(salva.getCodigo())).get().satisfies(item -> {
            assertThat(item.getConteudo()).isEqualTo("Notificacao de teste");
            assertThat(item.getSubprocesso().getCodigo()).isEqualTo(60000L);
        });
    }
}
