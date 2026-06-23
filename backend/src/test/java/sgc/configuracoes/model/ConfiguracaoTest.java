package sgc.configuracoes.model;

import org.junit.jupiter.api.*;
import sgc.configuracoes.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Configuracao")
class ConfiguracaoTest {

    @Test
    @DisplayName("deve atualizar descricao e valor a partir do request")
    void deveAtualizarDescricaoEValorAPartirDoRequest() {
        Configuracao configuracao = new Configuracao();
        configuracao.setDescricao("Anterior");
        configuracao.setValor("A");

        configuracao.atualizarDe(new ConfiguracaoRequest(1L, "chave", "Nova descrição", "B"));

        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getValor()).isEqualTo("B");
    }
}
