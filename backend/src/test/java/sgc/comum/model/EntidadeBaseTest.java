package sgc.comum.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EntidadeBase")
@Tag("unit")
class EntidadeBaseTest {
    private static class EntidadeTeste extends EntidadeBase {
        public EntidadeTeste(Long codigo) {
            super(codigo);
        }
    }

    @Test
    @DisplayName("toString() deve retornar representação JSON válida")
    void testToString() {
        EntidadeTeste entidade = new EntidadeTeste(123L);
        String resultado = entidade.toString();
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("\"codigo\":123"), "toString deve conter o código em formato JSON");
    }
}
