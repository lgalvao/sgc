package sgc.seguranca.config;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes de JwtProperties")
class JwtPropertiesTest {

    @Test
    @DisplayName("Deve usar expiração padrão quando valor for menor ou igual a zero")
    void deveUsarExpiracaoPadrao() {
        JwtProperties props = new JwtProperties("secret", 0);
        assertThat(props.expiracaoMinutos()).isEqualTo(120);

        JwtProperties propsNeg = new JwtProperties("secret", -1);
        assertThat(propsNeg.expiracaoMinutos()).isEqualTo(120);
    }

    @Test
    @DisplayName("Deve usar expiração informada quando valor for maior que zero")
    void deveUsarExpiracaoInformada() {
        JwtProperties props = new JwtProperties("secret", 60);
        assertThat(props.expiracaoMinutos()).isEqualTo(60);
    }
}
