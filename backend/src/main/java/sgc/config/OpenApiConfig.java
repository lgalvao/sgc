package sgc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI(
            @Value("${springdoc.info.title:SGC API}") String title,
            @Value("${springdoc.info.description:Sistema de Gestão de Competências}") String description,
            @Value("${springdoc.info.version:v1}") String version) {
        return new OpenAPI().info(new Info()
                .title(title)
                .description(description)
                .version(version)
        );
    }
}
