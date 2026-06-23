package sgc.seguranca.config;

import org.springframework.boot.context.properties.*;

import java.util.*;

/**
 * Propriedades de configuração CORS.
 *
 * <p>Record imutável com valores default via compact constructor.
 */
@ConfigurationProperties(prefix = "aplicacao.cors")
public record ConfigCorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials
) {
    private static final List<String> DEFAULT_ORIGINS = List.of("http://localhost:5173");
    private static final List<String> DEFAULT_METHODS = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    private static final List<String> DEFAULT_HEADERS = List.of("*");

    public ConfigCorsProperties {
        allowedOrigins = allowedOrigins != null ? allowedOrigins : DEFAULT_ORIGINS;
        allowedMethods = allowedMethods != null ? allowedMethods : DEFAULT_METHODS;
        allowedHeaders = allowedHeaders != null ? allowedHeaders : DEFAULT_HEADERS;
    }
}
