package sgc.configuracoes.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("ConfiguracaoRepo - Testes de Repositório")
class ConfiguracaoRepoTest {

    @Autowired
    private ConfiguracaoRepo configuracaoRepo;

    @Test
    @DisplayName("deve buscar parametro por chave")
    void deveBuscarParametroPorChave() {
        configuracaoRepo.save(Configuracao.builder()
                .chave("DIAS_ALERTA_NOVO")
                .descricao("Dias para indicacao de alerta como novo")
                .valor("3")
                .build());

        assertThat(configuracaoRepo.findByChave("DIAS_ALERTA_NOVO")).get().satisfies(parametro -> {
            assertThat(parametro.getDescricao()).contains("alerta");
            assertThat(parametro.getValor()).isEqualTo("3");
        });
    }
}
