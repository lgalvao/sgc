package sgc.seguranca.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class JwtPropertiesCoverageTest {

    @Test
    @DisplayName("Deve usar expiração padrão quando valor configurado for zero ou negativo")
    void deveUsarExpiracaoPadrao() {
        JwtProperties propsZero = new JwtProperties("secret", 0);
        assertThat(propsZero.expiracaoMinutos()).isEqualTo(120);

        JwtProperties propsNegative = new JwtProperties("secret", -10);
        assertThat(propsNegative.expiracaoMinutos()).isEqualTo(120);
    }

    @Test
    @DisplayName("Deve usar expiração configurada quando positiva")
    void deveUsarExpiracaoConfigurada() {
        JwtProperties props = new JwtProperties("secret", 60);
        assertThat(props.expiracaoMinutos()).isEqualTo(60);
    }
}
