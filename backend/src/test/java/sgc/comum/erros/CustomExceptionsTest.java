package sgc.comum.erros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CustomExceptionsTest {

    // Classe de teste para cobrir o construtor com causa de ErroInterno
    private static class ErroInternoTeste extends ErroInterno {
        public ErroInternoTeste(String message) {
            super(message);
        }
        
        public ErroInternoTeste(String message, Throwable cause) {
            super(message, cause);
        }
    }

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

    @Test
    @DisplayName("Deve instanciar ErroConfiguracao (subclasse de ErroInterno)")
    void testErroConfiguracao() {
        String message = "Erro de configuração";
        ErroConfiguracao exception = new ErroConfiguracao(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Deve instanciar ErroInvarianteViolada (subclasse de ErroInterno)")
    void testErroInvarianteViolada() {
        String message = "Invariante violada";
        ErroInvarianteViolada exception = new ErroInvarianteViolada(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("ErroInterno deve suportar construtor com mensagem e causa")
    void testErroInternoComCausa() {
        String message = "Erro interno";
        Throwable cause = new RuntimeException("Causa raiz");
        ErroInternoTeste exception = new ErroInternoTeste(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("ErroNegocio deve retornar null para getDetails() por padrão")
    void testErroNegocioGetDetailsDefault() {
        // Usando ErroValidacao que implementa ErroNegocio
        ErroValidacao exception = new ErroValidacao("Validação falhou");
        assertEquals(null, exception.getDetails());
    }
}
