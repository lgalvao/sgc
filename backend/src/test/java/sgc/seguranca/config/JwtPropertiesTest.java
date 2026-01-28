package sgc.seguranca.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testes de JwtProperties")
class JwtPropertiesTest {

    @Test
    @DisplayName("Deve usar expiração padrão quando valor for menor ou igual a zero")
    void deveUsarExpiracaoPadrao() {
        JwtProperties props = new JwtProperties("secret", 0);
        assertEquals(120, props.expiracaoMinutos());

        JwtProperties propsNeg = new JwtProperties("secret", -1);
        assertEquals(120, propsNeg.expiracaoMinutos());
    }

    @Test
    @DisplayName("Deve usar expiração informada quando valor for maior que zero")
    void deveUsarExpiracaoInformada() {
        JwtProperties props = new JwtProperties("secret", 60);
        assertEquals(60, props.expiracaoMinutos());
    }
}
