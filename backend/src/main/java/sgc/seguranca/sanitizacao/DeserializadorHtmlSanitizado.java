package sgc.seguranca.sanitizacao;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

/**
 * Deserializador JSON customizado que sanitiza conteúdo HTML em campos String.
 * Previne ataques XSS removendo HTML/JavaScript potencialmente malicioso.
 */
public class DeserializadorHtmlSanitizado extends StdDeserializer<String> {

    public DeserializadorHtmlSanitizado() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return value;
        }
        return UtilSanitizacao.sanitizar(value);
    }
}
