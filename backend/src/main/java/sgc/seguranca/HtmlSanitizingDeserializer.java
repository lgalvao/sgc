package sgc.seguranca;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Custom JSON deserializer that sanitizes HTML content in String fields. Prevents XSS attacks by
 * removing potentially malicious HTML/JavaScript.
 */
public class HtmlSanitizingDeserializer extends StdDeserializer<String> {

    public HtmlSanitizingDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return value;
        }
        return SanitizacaoUtil.sanitizar(value);
    }
}
