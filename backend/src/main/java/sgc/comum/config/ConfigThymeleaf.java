package sgc.comum.config;

import org.springframework.context.annotation.*;
import org.thymeleaf.spring6.*;
import org.thymeleaf.templatemode.*;
import org.thymeleaf.templateresolver.*;

/**
 * Configuração explícita do Thymeleaf para garantir o carregamento correto dos templates de e-mail.
 */
@Configuration
public class ConfigThymeleaf {

    @Bean
    public SpringTemplateEngine springTemplateEngine() {
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(templateResolver());
        return springTemplateEngine;
    }

    @Bean
    public ITemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/email/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        resolver.setCheckExistence(true);
        resolver.setOrder(1);
        return resolver;
    }
}
