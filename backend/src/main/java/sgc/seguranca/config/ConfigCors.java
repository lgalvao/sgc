package sgc.seguranca.config;

import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;

/**
 * Configuração do bean CORS.
 *
 * <p>Separado de {@link ConfigCorsProperties} para manter Single Responsibility:
 * propriedades em record imutável, bean em classe de configuração.
 */
@Configuration
public class ConfigCors {

    private final ConfigCorsProperties properties;

    public ConfigCors(ConfigCorsProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(properties.allowedMethods());
        configuration.setAllowedHeaders(properties.allowedHeaders());
        configuration.setAllowCredentials(properties.allowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
