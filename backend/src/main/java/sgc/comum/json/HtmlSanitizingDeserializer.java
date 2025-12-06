package sgc.comum.json;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Custom JSON deserializer that sanitizes HTML content in String fields.
 * Prevents XSS attacks by removing potentially malicious HTML/JavaScript.
 */
public class HtmlSanitizingDeserializer extends StdDeserializer<String> {
    private static final PolicyFactory HTML_POLICY = new HtmlPolicyBuilder().toFactory();

    public HtmlSanitizingDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = p.getValueAsString();
        if (value == null || value.isBlank()) {
            return value;
        }
        return HTML_POLICY.sanitize(value);
    }
}
