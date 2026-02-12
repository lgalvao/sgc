package sgc.comum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigOpenApi {
    @Bean
    public OpenAPI customOpenAPI(
            @Value("${aplicacao.openapi.title:SGC API}") String title,
            @Value("${aplicacao.openapi.description:Sistema de Gestão de Competências}") String description,
            @Value("${aplicacao.openapi.version:v1}") String version) {
        return new OpenAPI().info(new Info()
                .title(title)
                .description(description)
                .version(version)
        );
    }
}
