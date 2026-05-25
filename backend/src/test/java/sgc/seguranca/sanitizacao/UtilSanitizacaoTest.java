package sgc.seguranca.sanitizacao;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class UtilSanitizacaoTest {
    @Test
    @DisplayName("Deve manter texto limpo")
    void deveManterTextoLimpo() {
        String input = "Texto normal";
        assertThat(UtilSanitizacao.sanitizar(input)).isEqualTo(input);
    }

    @Test
    @DisplayName("Deve remover script tags")
    void deveRemoverScriptTags() {
        String input = "Texto <script>alert('xss')</script>";
        String expected = "Texto ";
        assertThat(UtilSanitizacao.sanitizar(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve remover tags html mas manter conteudo se politica padrao for usada")
    void deveRemoverTagsHtml() {
        String input = "<b>Negrito</b>";
        String expected = "Negrito";
        assertThat(UtilSanitizacao.sanitizar(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve permitir formatação HTML básica permitida")
    void devePermitirFormatacaoBasica() {
        String input = "<p>Olá <strong>mundo</strong></p>";
        assertThat(UtilSanitizacao.sanitizarFormatado(input)).isEqualTo(input);
    }

    @Test
    @DisplayName("Deve remover script tags e tags não permitidas em sanitizarFormatado")
    void deveRemoverScriptTagsETagsNaoPermitidasFormatado() {
        String input = "<h1>Título</h1><p>Texto</p><script>alert('xss')</script>";
        String expected = "Título<p>Texto</p>";
        assertThat(UtilSanitizacao.sanitizarFormatado(input)).isEqualTo(expected);
    }
}
