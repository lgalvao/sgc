package sgc.integracao.mocks;

import org.springframework.beans.factory.ObjectProvider;
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
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.seguranca.login.FiltroJwt;
import sgc.seguranca.login.GerenciadorJwt;
import sgc.seguranca.login.ListaNegraJwt;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Import(SgcPermissionEvaluator.class)
public class TestSecurityConfig {
    @Bean
    @ConditionalOnBean(GerenciadorJwt.class)
    public FiltroJwt filtroJwt(GerenciadorJwt gerenciadorJwt, UsuarioAplicacaoService usuarioAplicacaoService, ListaNegraJwt listaNegraJwt) {
        return new FiltroJwt(gerenciadorJwt, usuarioAplicacaoService, listaNegraJwt);
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
    public SecurityFilterChain testFilterChain(HttpSecurity http, ObjectProvider<FiltroJwt> filtroJwtProvider) {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuarios/login", "/api/usuarios/entrar", "/api/usuarios/logout").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        filtroJwtProvider.ifAvailable(filtroJwt ->
                http.addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
        );

        return http.build();
    }
}
