package sgc.comum.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Sleeper")
@Tag("unit")
class SleeperTest {

    @Test
    @DisplayName("sleep() deve pausar a execução pelo tempo especificado")
    void testSleep() throws InterruptedException {
        Sleeper sleeper = new Sleeper();
        long inicio = System.currentTimeMillis();
        sleeper.sleep(100);
        long duracao = System.currentTimeMillis() - inicio;

        // Verifica que dormiu pelo menos 90ms (margem para variação do sistema)
        assertTrue(duracao >= 90, "Deve ter dormido por pelo menos 90ms");
    }

    @Test
    @DisplayName("sleep() deve propagar InterruptedException")
    void testSleepInterrupted() {
        Sleeper sleeper = new Sleeper();

        // Interrompe a thread antes de chamar sleep
        Thread.currentThread().interrupt();

        assertThrows(InterruptedException.class, () -> sleeper.sleep(1000));
    }
}
