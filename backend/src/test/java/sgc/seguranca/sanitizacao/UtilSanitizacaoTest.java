package sgc.seguranca.sanitizacao;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UtilSanitizacaoTest {
    @Test
    @DisplayName("Deve manter texto limpo")
    void deveManterTextoLimpo() {
        String input = "Texto normal";
        assertEquals(input, UtilSanitizacao.sanitizar(input));
    }

    @Test
    @DisplayName("Deve remover script tags")
    void deveRemoverScriptTags() {
        String input = "Texto <script>alert('xss')</script>";
        String expected = "Texto ";
        assertEquals(expected, UtilSanitizacao.sanitizar(input));
    }

    @Test
    @DisplayName("Deve remover tags html mas manter conteudo se politica padrao for usada")
    void deveRemoverTagsHtml() {
        String input = "<b>Negrito</b>";
        String expected = "Negrito"; 
        assertEquals(expected, UtilSanitizacao.sanitizar(input));
    }
}
