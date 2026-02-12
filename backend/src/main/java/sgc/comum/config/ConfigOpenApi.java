package sgc.comum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ConfigOpenApi {
    private final ConfigAplicacao config;

    @Bean
    public OpenAPI customOpenAPI() {
        var openapi = config.getOpenapi();
        return new OpenAPI().info(new Info()
                .title(openapi.getTitle() != null ? openapi.getTitle() : "SGC API")
                .description(openapi.getDescription() != null ? openapi.getDescription() : "Sistema de Gestão de Competências")
                .version(openapi.getVersion() != null ? openapi.getVersion() : "v1")
        );
    }
}
