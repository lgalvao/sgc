package sgc.seguranca;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom JSON deserializer that sanitizes HTML content in String fields. Prevents XSS attacks by
 * removing potentially malicious HTML/JavaScript.
 */
public class HtmlSanitizingDeserializer extends StdDeserializer<String> {

    public HtmlSanitizingDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return value;
        }
        return SanitizacaoUtil.sanitizar(value);
    }
}
