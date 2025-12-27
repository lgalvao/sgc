package sgc.seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sgc.seguranca.LimitadorTentativasLogin.ErroMuitasTentativas;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LimitadorTentativasLoginTest {

    private LimitadorTentativasLogin limitador;

    @BeforeEach
    void setUp() {
        limitador = new LimitadorTentativasLogin();
    }

    @Test
    void devePermitirAteLimiteDeTentativas() {
        String ip = "192.168.1.1";

        // 5 tentativas permitidas
        IntStream.range(0, 5).forEach(i ->
            assertDoesNotThrow(() -> limitador.verificar(ip))
        );
    }

    @Test
    void deveBloquearAposLimiteExcedido() {
        String ip = "192.168.1.2";

        // Consome as 5 tentativas
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip));

        // 6ª tentativa deve falhar
        assertThrows(ErroMuitasTentativas.class, () -> limitador.verificar(ip));
    }

    @Test
    void deveIgnorarIpNulo() {
        assertDoesNotThrow(() -> limitador.verificar(null));
    }

    @Test
    void naoDeveBloquearIpsDiferentes() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // Bloqueia ip1
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip1));
        assertThrows(ErroMuitasTentativas.class, () -> limitador.verificar(ip1));

        // ip2 deve continuar livre
        assertDoesNotThrow(() -> limitador.verificar(ip2));
    }
}
