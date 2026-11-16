package sesel.sgc.processo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessoNaoEncontradoErroTest {
    @Test
    void deveInstanciarEArmazenarMensagem() {
        ProcessoNaoEncontradoErro ex = new ProcessoNaoEncontradoErro("processo não encontrado");
        assertTrue(ex.getMessage().contains("processo não encontrado"));
    }

    @Test
    void deveLancarExcecao() {
        assertThrows(ProcessoNaoEncontradoErro.class, () -> {
            throw new ProcessoNaoEncontradoErro("erro");
        });
    }
}