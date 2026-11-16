package sesel.sgc.mapa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ValidacaoErroTest {
    @Test
    void testConstrutorComMensagem() {
        String msg = "Erro de negócio";
        ValidacaoErro ex = new ValidacaoErro(msg);
        assertEquals(msg, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstrutorComMensagemECausa() {
        String msg = "Erro de negócio";
        Throwable causa = new RuntimeException("Causa raiz");
        ValidacaoErro ex = new ValidacaoErro(msg, causa);
        assertEquals(msg, ex.getMessage());
        assertEquals(causa, ex.getCause());
    }
}