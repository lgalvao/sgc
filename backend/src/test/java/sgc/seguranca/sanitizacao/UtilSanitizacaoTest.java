package sgc.seguranca.sanitizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilSanitizacaoTest {

    @Test
    @DisplayName("Deve retornar nulo se entrada for nula")
    void deveRetornarNuloSeEntradaNula() {
        assertNull(UtilSanitizacao.sanitizar(null));
    }

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
        // OWASP sanitizer usually removes the tag and its content if it's unsafe
        // Note: behavior depends on policy. The default policy in UtilSanitizacao is likely strict.
        // Let's assume it removes the script tag entirely.
        String expected = "Texto ";
        assertEquals(expected, UtilSanitizacao.sanitizar(input));
    }

    @Test
    @DisplayName("Deve remover tags html mas manter conteudo se politica padrao for usada")
    void deveRemoverTagsHtml() {
        // Since UtilSanitizacao uses HtmlPolicyBuilder().toFactory() without configuration,
        // it likely produces a "deny all" policy or a very basic one.
        // If it's "new HtmlPolicyBuilder().toFactory()", it allows NOTHING.
        String input = "<b>Negrito</b>";
        String expected = "Negrito"; 
        assertEquals(expected, UtilSanitizacao.sanitizar(input));
    }
}
