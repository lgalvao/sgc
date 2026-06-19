package sgc.configuracoes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.configuracoes.ConfiguracaoRequest;

import static org.assertj.core.api.Assertions.assertThat;

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
