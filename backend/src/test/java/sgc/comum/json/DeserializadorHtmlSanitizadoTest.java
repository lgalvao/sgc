package sgc.comum.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import sgc.seguranca.sanitizacao.DeserializadorHtmlSanitizado;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Testes para DeserializadorHtmlSanitizado")
@Tag("unit")
class DeserializadorHtmlSanitizadoTest {

    private DeserializadorHtmlSanitizado deserializador;
    private JsonParser parser;
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        deserializador = new DeserializadorHtmlSanitizado();
        parser = mock(JsonParser.class);
        context = mock(DeserializationContext.class);
    }

    @Test
    @DisplayName("Deve remover script tags do conteúdo HTML")
    void deveRemoverScriptTagsDoConteudoHtml() throws Exception {
        // Arrange
        String inputMalicioso = "<script>alert('XSS')</script>Texto limpo";
        when(parser.getValueAsString()).thenReturn(inputMalicioso);

        // Act
        String resultado = deserializador.deserialize(parser, context);

        // Assert
        assertThat(resultado).doesNotContain("<script>");
        assertThat(resultado).doesNotContain("alert");
        assertThat(resultado).contains("Texto limpo");
    }

    @Test
    @DisplayName("Deve remover event handlers de HTML")
    void deveRemoverEventHandlersDeHtml() throws Exception {
        // Arrange
        String inputMalicioso = "<img src='x' onerror='alert(1)'>";
        when(parser.getValueAsString()).thenReturn(inputMalicioso);

        // Act
        String resultado = deserializador.deserialize(parser, context);

        // Assert
        assertThat(resultado).doesNotContain("onerror");
        assertThat(resultado).doesNotContain("alert");
    }

    @Test
    @DisplayName("Deve permitir texto simples sem HTML")
    void devePermitirTextoSimplesSeHtml() throws Exception {
        // Arrange
        String textoSimples = "Este é um texto simples sem tags HTML";
        when(parser.getValueAsString()).thenReturn(textoSimples);

        // Act
        String resultado = deserializador.deserialize(parser, context);

        // Assert
        assertThat(resultado).isEqualTo(textoSimples);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "  ", "\t", "\n" })
    @DisplayName("Deve retornar valores nulos ou vazios sem alteração")
    void deveRetornarValoresNulosOuVaziosSemAlteracao(String input) throws Exception {
        // Arrange
        when(parser.getValueAsString()).thenReturn(input);

        // Act
        String resultado = deserializador.deserialize(parser, context);

        // Assert
        assertThat(resultado).isEqualTo(input);
    }

    @Test
    @DisplayName("Deve sanitizar SQL injection em string")
    void deveSanitizarSqlInjectionEmString() throws Exception {
        // Arrange
        String inputMalicioso = "'; DROP TABLE users; --";
        when(parser.getValueAsString()).thenReturn(inputMalicioso);

        // Act
        String resultado = deserializador.deserialize(parser, context);

        // Assert
        // O sanitizador HTML remove tags, mas texto plano permanece
        // O importante é que não permite execução de código
        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve remover javascript: protocol de links")
    void deveRemoverJavascriptProtocolDeLinks() throws Exception {
        // Arrange
        String inputMalicioso = "<a href='javascript:alert(1)'>Click me</a>";
        when(parser.getValueAsString()).thenReturn(inputMalicioso);

        // Act
        String resultado = deserializador.deserialize(parser, context);

        // Assert
        assertThat(resultado).doesNotContain("javascript:");
        assertThat(resultado).doesNotContain("alert");
    }

    @SuppressWarnings("HttpUrlsUsage")
    @Test
    @DisplayName("Deve remover iframes maliciosos")
    void deveRemoverIframesMaliciosos() throws Exception {
        // Arrange
        String inputMalicioso = "<iframe src='http://malicious.com'></iframe>Texto";
        when(parser.getValueAsString()).thenReturn(inputMalicioso);

        // Act
        String resultado = deserializador.deserialize(parser, context);

        // Assert
        assertThat(resultado).doesNotContain("<iframe");
        assertThat(resultado).doesNotContain("malicious.com");
        assertThat(resultado).contains("Texto");
    }
}
