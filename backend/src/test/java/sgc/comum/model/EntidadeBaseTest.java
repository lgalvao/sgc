package sgc.comum.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EntidadeBaseTest")
class EntidadeBaseTest {

    private static class EntidadeTeste extends EntidadeBase {
    }

    @Test
    @DisplayName("deve gerar toString com nome da classe e código")
    void deveGerarToStringComNomeClasseECodigo() {
        EntidadeTeste entidade = new EntidadeTeste();
        entidade.setCodigo(42L);

        assertThat(entidade.toString()).isEqualTo("EntidadeTeste[codigo=42]");
    }
}
