package sgc.seguranca.sanitizacao;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializador JSON customizado que sanitiza conteúdo HTML em campos String,
 * preservando a formatação básica permitida para evitar ataques XSS.
 */
public class DeserializadorHtmlFormatadoSanitizado extends StdDeserializer<String> {

    public DeserializadorHtmlFormatadoSanitizado() {
        super(String.class);
    }

    @Override
    public @Nullable String deserialize(JsonParser parser, DeserializationContext ctxt) {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return value;
        }
        return UtilSanitizacao.sanitizarFormatado(value);
    }
}
