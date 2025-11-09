package sgc.comum.erros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomExceptionsTest {
    @Test
    @DisplayName("Deve instanciar ErroAccessoNegado")
    void testErroDominioAccessoNegado() {
        String message = "Acesso negado";
        ErroAccessoNegado exception = new ErroAccessoNegado(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Deve instanciar ErroEntidadeNaoEncontrada")
    void testErroDominioNaoEncontrado() {
        String message = "Dominio não encontrado";
        ErroEntidadeNaoEncontrada exception = new ErroEntidadeNaoEncontrada(message);
        assertEquals(message, exception.getMessage());
    }
}
