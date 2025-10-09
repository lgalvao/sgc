package sgc.comum.erros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionsTest {

    @Test
    @DisplayName("Deve instanciar ErroDominioAccessoNegado")
    void testErroDominioAccessoNegado() {
        String message = "Acesso negado";
        ErroDominioAccessoNegado exception = new ErroDominioAccessoNegado(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Deve instanciar ErroDominioNaoEncontrado")
    void testErroDominioNaoEncontrado() {
        String message = "Dominio não encontrado";
        ErroDominioNaoEncontrado exception = new ErroDominioNaoEncontrado(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Deve instanciar ErroEntidadeNaoEncontrada")
    void testErroEntidadeNaoEncontrada() {
        String message = "Entidade não encontrada";
        ErroEntidadeNaoEncontrada exception = new ErroEntidadeNaoEncontrada(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Deve instanciar ErroServicoExterno com mensagem")
    void testErroServicoExternoWithMessage() {
        String message = "Erro de serviço externo";
        ErroServicoExterno exception = new ErroServicoExterno(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve instanciar ErroServicoExterno com mensagem e causa")
    void testErroServicoExternoWithMessageAndCause() {
        String message = "Erro de serviço externo";
        Throwable cause = new RuntimeException("Causa raiz");
        ErroServicoExterno exception = new ErroServicoExterno(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
