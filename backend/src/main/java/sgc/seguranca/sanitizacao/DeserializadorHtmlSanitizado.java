package sgc.seguranca.sanitizacao;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializador JSON customizado que sanitiza conteúdo HTML em campos String.
 * Previne ataques XSS removendo HTML/JavaScript potencialmente malicioso.
 */
public class DeserializadorHtmlSanitizado extends StdDeserializer<String> {

    public DeserializadorHtmlSanitizado() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return value;
        }
        return UtilSanitizacao.sanitizar(value);
    }
}
