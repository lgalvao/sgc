package sesel.sgc;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config implements ErrorPageRegistrar, WebMvcConfigurer {
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        registry.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/acesso-negado"));
    }
}
