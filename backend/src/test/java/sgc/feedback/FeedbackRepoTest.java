package sgc.feedback;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
                .usuarioCodigo("123")
                .usuarioNome("Testador")
                .enviadoEm(OffsetDateTime.now())
                .rota("/teste")
                .status(FeedbackStatus.NOVO)
                .build();

        FeedbackRegistro salvo = repo.save(registro);
        assertThat(salvo.getCodigo()).isNotNull();

        Optional<FeedbackRegistro> recuperado = repo.findById(salvo.getCodigo());
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getNota()).isEqualTo("Teste de repositório");
    }
}
