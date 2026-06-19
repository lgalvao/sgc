package sgc.seguranca.sanitizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("DeserializadorHtmlSanitizado - deve lidar com valores nulos, vazios ou normais")
    void testDeserializadorHtmlSanitizado() throws java.io.IOException {
        DeserializadorHtmlSanitizado deserializador = new DeserializadorHtmlSanitizado();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);

        // Caso 1: Nulo
        when(parser.getValueAsString()).thenReturn(null);
        assertThat(deserializador.deserialize(parser, ctxt)).isNull();

        // Caso 2: Em branco
        when(parser.getValueAsString()).thenReturn("   ");
        assertThat(deserializador.deserialize(parser, ctxt)).isEqualTo("   ");

        // Caso 3: HTML
        when(parser.getValueAsString()).thenReturn("<p>Texto</p>");
        assertThat(deserializador.deserialize(parser, ctxt)).isEqualTo("Texto");
    }

    @Test
    @DisplayName("DeserializadorHtmlFormatadoSanitizado - deve lidar com valores nulos, vazios ou normais")
    void testDeserializadorHtmlFormatadoSanitizado() throws java.io.IOException {
        DeserializadorHtmlFormatadoSanitizado deserializador = new DeserializadorHtmlFormatadoSanitizado();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);

        // Caso 1: Nulo
        when(parser.getValueAsString()).thenReturn(null);
        assertThat(deserializador.deserialize(parser, ctxt)).isNull();

        // Caso 2: Em branco
        when(parser.getValueAsString()).thenReturn("   ");
        assertThat(deserializador.deserialize(parser, ctxt)).isEqualTo("   ");

        // Caso 3: HTML com formatação
        when(parser.getValueAsString()).thenReturn("<p><strong>Texto</strong></p>");
        assertThat(deserializador.deserialize(parser, ctxt)).isEqualTo("<p><strong>Texto</strong></p>");
    }
}
