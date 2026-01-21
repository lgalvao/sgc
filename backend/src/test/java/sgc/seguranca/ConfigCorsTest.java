package sgc.seguranca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import sgc.seguranca.config.ConfigCors;
import sgc.seguranca.config.ConfigCorsProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("ConfigCors - Testes de Configuração CORS")
class ConfigCorsTest {
    @Test
    @DisplayName("Deve configurar origem CORS com origens permitidas")
    void deveConfigurarOrigemCorsComOrigensPermitidas() {
        // Cria record de propriedades imutável
        ConfigCorsProperties properties = new ConfigCorsProperties(
            List.of("https://example.com"),
            List.of("GET", "POST"),
            List.of("*"),
            true
        );
        
        // Injeta no bean de configuração
        ConfigCors config = new ConfigCors(properties);

        CorsConfigurationSource source = config.corsConfigurationSource();
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/test");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("https://example.com");
        assertThat(configuration.getAllowedMethods()).containsExactly("GET", "POST");
        assertThat(configuration.getAllowedHeaders()).containsExactly("*");
        assertThat(configuration.getAllowCredentials()).isTrue();
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
