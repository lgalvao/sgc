package sgc.seguranca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import sgc.seguranca.config.ConfigCors;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("ConfigCors - Testes de Configuração CORS")
class ConfigCorsTest {
    @Test
    @DisplayName("Deve configurar origem CORS com origens permitidas")
    void deveConfigurarOrigemCorsComOrigensPermitidas() {
        ConfigCors config = new ConfigCors();
        config.setAllowedOrigins(List.of("http://example.com"));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        CorsConfigurationSource source = config.corsConfigurationSource();
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
