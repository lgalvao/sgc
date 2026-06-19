package sgc.comum.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MascaraUtil")
class MascaraUtilTest {

    @Test
    @DisplayName("deve retornar *** para valor nulo")
    void deveRetornarAsteriscoParaValorNulo() {
        assertThat(MascaraUtil.mascarar(null)).isEqualTo("***");
    }

    @Test
    @DisplayName("deve retornar *** para valor com ate 4 caracteres")
    void deveRetornarAsteriscoParaValorCurto() {
        assertThat(MascaraUtil.mascarar("1234")).isEqualTo("***");
        assertThat(MascaraUtil.mascarar("abc")).isEqualTo("***");
    }

    @Test
    @DisplayName("deve mascarar mantendo apenas os ultimos 4 caracteres")
    void deveMascararMantendoUltimos4Caracteres() {
        assertThat(MascaraUtil.mascarar("123456789012")).isEqualTo("***9012");
    }
}
