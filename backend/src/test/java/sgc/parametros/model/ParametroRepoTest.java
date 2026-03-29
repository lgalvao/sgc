package sgc.parametros.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("ParametroRepo - Testes de Repositório")
class ParametroRepoTest {

    @Autowired
    private ParametroRepo parametroRepo;

    @Test
    @DisplayName("deve buscar parametro por chave")
    void deveBuscarParametroPorChave() {
        parametroRepo.save(Parametro.builder()
                .chave("DIAS_ALERTA_NOVO")
                .descricao("Dias para indicacao de alerta como novo")
                .valor("3")
                .build());

        assertThat(parametroRepo.findByChave("DIAS_ALERTA_NOVO")).get().satisfies(parametro -> {
            assertThat(parametro.getDescricao()).contains("alerta");
            assertThat(parametro.getValor()).isEqualTo("3");
        });
    }
}
