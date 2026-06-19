package sgc.configuracoes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

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
