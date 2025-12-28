package sgc.seguranca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    @DisplayName("Should configure CORS source with allowed origins")
    void shouldConfigureCorsSource() {
        CorsConfig config = new CorsConfig();
        config.setAllowedOrigins(List.of("http://example.com"));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        CorsConfigurationSource source = config.corsConfigurationSource();
        // Since UrlBasedCorsConfigurationSource requires a request to resolve path patterns,
        // we can verify the source internal map indirectly or mock a request.
        // Or simply verify the configuration logic inside the bean method.
        // For unit testing the config bean itself, checking the created configuration is enough.

        // However, UrlBasedCorsConfigurationSource.getCorsConfiguration(request) logic is:
        // path = resolvePath(request);
        // return this.corsConfigurations.get(path);

        // A simpler test is to use reflection or check behavior.
        // But to keep it simple and safe, let's just inspect the CorsConfig object itself
        // and ensure the bean creation logic maps it correctly.

        // Let's create a MockHttpServletRequest to satisfy the source.
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/test");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("http://example.com");
        assertThat(configuration.getAllowedMethods()).containsExactly("GET", "POST");
        assertThat(configuration.getAllowedHeaders()).containsExactly("*");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
