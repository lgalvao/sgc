package sgc.comum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Configuração explícita do Thymeleaf para garantir o carregamento correto dos templates de e-mail.
 */
@Configuration
public class ThymeleafConfig {

    @Bean
    public ITemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); // Sem barra inicial para pegar do classpath root relativo ou absolute
        // Tentativa: classpath:templates/
        // O ClassLoaderTemplateResolver usa o classloader, então "templates/" é relativo ao root do classpath
        
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // Desativar cache para evitar problemas durante dev/test
        resolver.setCheckExistence(true);
        resolver.setOrder(1);
        return resolver;
    }
}
