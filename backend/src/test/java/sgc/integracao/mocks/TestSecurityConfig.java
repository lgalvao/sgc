package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.expression.method.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;
import sgc.organizacao.*;
import sgc.seguranca.*;
import sgc.seguranca.login.*;

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
