package sgc.comum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafConfig {
    /**
     * Configura o mecanismo de templates do Thymeleaf.
     * <p>
     * Registra o 'resolvedor' de templates de email para que o Thymeleaf
     * possa processar os templates localizados no classpath.
     *
     * @return a instância configurada do {@link SpringTemplateEngine}.
     */
    @Bean
    public SpringTemplateEngine springTemplateEngine() {
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(emailTemplateResolver());
        return springTemplateEngine;
    }

    /**
     * Configura o resolvedor de templates para emails.
     * <p>
     * Este método define a localização dos templates de email (dentro de
     * 'resources/templates/email/'), o sufixo dos arquivos ('.html'), o modo
     * do template (HTML5) e a codificação de caracteres (UTF-8).
     *
     * @return o {@link ClassLoaderTemplateResolver} configurado para os emails.
     */
    public ClassLoaderTemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
        emailTemplateResolver.setPrefix("/templates/email/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
        emailTemplateResolver.setCharacterEncoding("UTF-8");
        emailTemplateResolver.setOrder(1);
        emailTemplateResolver.setCheckExistence(true);
        return emailTemplateResolver;
    }
}