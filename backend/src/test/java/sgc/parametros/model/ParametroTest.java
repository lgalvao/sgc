package sgc.parametros.model;

import org.junit.jupiter.api.*;
import sgc.parametros.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Parametro")
class ParametroTest {

    @Test
    @DisplayName("deve atualizar descricao e valor a partir do request")
    void deveAtualizarDescricaoEValorAPartirDoRequest() {
        Parametro parametro = new Parametro();
        parametro.setDescricao("Anterior");
        parametro.setValor("A");

        parametro.atualizarDe(new ParametroRequest(1L, "chave", "Nova descrição", "B"));

        assertThat(parametro.getDescricao()).isEqualTo("Nova descrição");
        assertThat(parametro.getValor()).isEqualTo("B");
    }
}
