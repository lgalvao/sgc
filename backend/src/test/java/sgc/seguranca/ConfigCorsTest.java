package sgc.seguranca;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import sgc.seguranca.config.ConfigCors;
import sgc.seguranca.config.ConfigCorsProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfigCors - Testes de Configuração CORS")
class ConfigCorsTest {
    private static @Nullable CorsConfiguration createCorsConfiguration() {
        ConfigCorsProperties properties = new ConfigCorsProperties(
                List.of("https://example.com"),
                List.of("GET", "POST"),
                List.of("*"),
                true
        );

        // Injeta no bean de configuração
        ConfigCors config = new ConfigCors(properties);

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        return source.getCorsConfiguration(request);
    }

    @Test
    @DisplayName("Deve configurar origem CORS com origens permitidas")
    void deveConfigurarOrigemCorsComOrigensPermitidas() {
        CorsConfiguration configuration = createCorsConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("https://example.com");
        assertThat(configuration.getAllowedMethods()).containsExactly("GET", "POST");
        assertThat(configuration.getAllowedHeaders()).containsExactly("*");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("Nao deve aplicar CORS para recursos estaticos")
    void naoDeveAplicarCorsParaRecursosEstaticos() {
        ConfigCorsProperties properties = new ConfigCorsProperties(
                List.of("https://example.com"),
                List.of("GET", "POST"),
                List.of("*"),
                true
        );
        ConfigCors config = new ConfigCors(properties);

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/assets/index.js");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNull();
    }

    @Test
    @DisplayName("Deve usar valores padrão quando propriedades são null")
    void deveUsarValoresPadraoQuandoPropriedadesSaoNull() {
        // Compact constructor aplica valores default
        ConfigCorsProperties properties = new ConfigCorsProperties(null, null, null, false);

        assertThat(properties.allowedOrigins()).containsExactly("http://localhost:5173");
        assertThat(properties.allowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(properties.allowedHeaders()).containsExactly("*");
    }
}
