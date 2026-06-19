package sgc.comum.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EntidadeBaseTest")
class EntidadeBaseTest {

    @Test
    @DisplayName("deve gerar toString com nome da classe e código")
    void deveGerarToStringComNomeClasseECodigo() {
        EntidadeTeste entidade = new EntidadeTeste();
        entidade.setCodigo(42L);

        assertThat(entidade).hasToString("EntidadeTeste[codigo=42]");
    }

    @Test
    @DisplayName("deve comparar igualdade por código")
    void deveCompararIgualdadePorCodigo() {
        EntidadeTeste primeira = new EntidadeTeste();
        primeira.setCodigo(42L);
        EntidadeTeste segunda = new EntidadeTeste();
        segunda.setCodigo(42L);

        assertThat(primeira)
                .isEqualTo(segunda)
                .hasSameHashCodeAs(segunda);
    }

    @Test
    @DisplayName("não deve considerar igual quando código for nulo ou tipo for diferente")
    void naoDeveConsiderarIgualQuandoCodigoNuloOuTipoDiferente() {
        EntidadeTeste entidade = new EntidadeTeste();

        assertThat(entidade)
                .isNotEqualTo(new Object())
                .isNotEqualTo(new EntidadeTeste());
    }

    private static class EntidadeTeste extends EntidadeBase {
    }
}
