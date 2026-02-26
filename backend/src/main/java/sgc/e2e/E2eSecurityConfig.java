package sgc.e2e;

import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.expression.method.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;
import org.springframework.web.cors.*;
import sgc.organizacao.*;
import sgc.seguranca.*;
import sgc.seguranca.login.*;

import java.util.*;

/**
 * Configuração de segurança específica para testes E2E.
 * <p>
 * Similar à configuração padrão, mas permite acesso aos endpoints /e2e/** sem
 * autenticação. Os métodos de fixture usam SecurityContext interno para chamar métodos
 * com @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("e2e")
public class E2eSecurityConfig {

    @Bean
    public FiltroJwt filtroJwt(GerenciadorJwt gerenciadorJwt, UsuarioFacade usuarioFacade) {
        return new FiltroJwt(gerenciadorJwt, usuarioFacade);
    }

    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler(SgcPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    /**
     * Configura a cadeia de filtros de segurança para testes E2E.
     *
     * <p>
     * Permite acesso aos endpoints de fixture E2E sem autenticação, mas mantém
     * a autenticação para outros endpoints da API.
     */
    @Bean
    public SecurityFilterChain e2eSecurityFilterChain(HttpSecurity http, FiltroJwt filtroJwt) {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/api/usuarios/autenticar",
                                "/api/usuarios/autorizar",
                                "/api/usuarios/entrar",
                                "/actuator/**",
                                "/e2e/**")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
