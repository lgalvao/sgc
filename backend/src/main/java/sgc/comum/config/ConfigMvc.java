package sgc.comum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Configuração para suportar roteamento de Single Page Application (SPA).
 * Redireciona rotas que não são da API nem de arquivos estáticos para o index.html.
 */
@Configuration
public class ConfigMvc implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);

                        // Se o recurso existe e é legível, retorna ele (ex: .js, .css, imagens)
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // Se a rota começa com "api" ou "actuator", deixa o Spring tratar normalmente (ou dar 404/401)
                        if (resourcePath.startsWith("api") || resourcePath.startsWith("actuator")) {
                            return null;
                        }

                        // Para qualquer outra rota (ex: /login, /mapas), serve o index.html para o Vue Router assumir
                        return location.createRelative("index.html");
                    }
                });
    }
}
