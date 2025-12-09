package sgc.comum.json;

import net.jqwik.api.*;
import org.mockito.Mockito;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class HtmlSanitizingDeserializerPropertyTest {

    private final HtmlSanitizingDeserializer deserializer = new HtmlSanitizingDeserializer();

    @Property
    void deveSanitizarQualquerString(@ForAll String input) {
        // Setup
        JsonParser jsonParser = Mockito.mock(JsonParser.class);
        DeserializationContext context = Mockito.mock(DeserializationContext.class);

        when(jsonParser.getValueAsString()).thenReturn(input);

        // Execute
        String result = deserializer.deserialize(jsonParser, context);

        // Verify
        if (input == null || input.isBlank()) {
            assertThat(result).isEqualTo(input);
        } else {
            assertThat(result).doesNotContain("<script");
            assertThat(result).doesNotContain("javascript:");
        }
    }

    @Property
    void deveRemoverTagsEspecificas(
            @ForAll("htmlTags") String tag, @ForAll("textoSimples") String content) {
        JsonParser jsonParser = Mockito.mock(JsonParser.class);
        DeserializationContext context = Mockito.mock(DeserializationContext.class);

        String tagName = tag.substring(1, tag.length() - 1);
        String closingTag = "</" + tagName + ">";
        String input = tag + content + closingTag;

        when(jsonParser.getValueAsString()).thenReturn(input);

        String result = deserializer.deserialize(jsonParser, context);

        assertThat(result).doesNotContain(tag);
        assertThat(result).doesNotContain(closingTag);
        assertThat(result).isEqualTo(content);
    }

    @Provide
    Arbitrary<String> htmlTags() {
        return Arbitraries.of("<div>", "<span>", "<p>", "<b>", "<i>");
    }

    @Provide
    Arbitrary<String> textoSimples() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1);
    }

    @Property
    void naoDeveAlterarTextoSemHtml(@ForAll("textoSimples") String input) {
        JsonParser jsonParser = Mockito.mock(JsonParser.class);
        DeserializationContext context = Mockito.mock(DeserializationContext.class);

        when(jsonParser.getValueAsString()).thenReturn(input);
        String result = deserializer.deserialize(jsonParser, context);
        assertThat(result).isEqualTo(input);
    }
}
