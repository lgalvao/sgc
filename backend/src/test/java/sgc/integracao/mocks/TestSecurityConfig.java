package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sgc.organizacao.UsuarioFacade;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.seguranca.login.FiltroJwt;
import sgc.seguranca.login.GerenciadorJwt;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Import(SgcPermissionEvaluator.class)
public class TestSecurityConfig {
    @Bean
    @ConditionalOnBean(GerenciadorJwt.class)
    public FiltroJwt filtroJwt(GerenciadorJwt gerenciadorJwt, UsuarioFacade usuarioFacade) {
        return new FiltroJwt(gerenciadorJwt, usuarioFacade);
    }

    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler(SgcPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    @Bean
    @Primary
    @Profile({"test"})
    public SecurityFilterChain testFilterChain(HttpSecurity http, @Autowired(required = false) FiltroJwt filtroJwt) {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuarios/autenticar", "/api/usuarios/autorizar", "/api/usuarios/entrar").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        if (filtroJwt != null) {
            http.addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
