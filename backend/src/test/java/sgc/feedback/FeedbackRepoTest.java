package sgc.feedback;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("FeedbackRepo")
class FeedbackRepoTest {

    @Autowired
    private FeedbackRepo repo;

    @Test
    @DisplayName("deve salvar e recuperar um feedback")
    void deveSalvarERecuperar() {
        FeedbackRegistro registro = FeedbackRegistro.builder()
                .tipo(FeedbackTipo.BUG)
                .nota("Teste de repositório")
                .usuarioId("123")
                .usuarioNome("Testador")
                .enviadoEm(OffsetDateTime.now())
                .rota("/teste")
                .status(FeedbackStatus.NOVO)
                .build();

        FeedbackRegistro salvo = repo.save(registro);
        assertThat(salvo.getId()).isNotNull();

        Optional<FeedbackRegistro> recuperado = repo.findById(salvo.getId());
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getNota()).isEqualTo("Teste de repositório");
    }
}
