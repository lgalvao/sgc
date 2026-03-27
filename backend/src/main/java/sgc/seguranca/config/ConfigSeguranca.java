package sgc.seguranca.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.expression.method.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.csrf.*;
import org.springframework.web.filter.*;
import org.springframework.web.cors.*;
import sgc.organizacao.*;
import sgc.seguranca.*;
import sgc.seguranca.login.*;

import java.io.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("(!test & !e2e) | secure-test")
public class ConfigSeguranca {

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
     * Configura a cadeia de filtros de segurança para a aplicação.
     *
     * <p>
     * Esta configuração define as seguintes regras:
     *
     * <ul>
     * <li>Permite acesso anônimo aos endpoints de autenticação.
     * <li>Exige autenticação para todos os outros endpoints sob '/api/'.
     * <li>Permite acesso a qualquer outra requisição (ex: frontend estático).
     * <li>Desabilita CSRF, HTTP Basic e formulário de login, adequando-se a uma API
     * RESTful.
     * <li>Retorna status 401 Unauthorized para tentativas de acesso não
     * autenticadas a endpoints
     * protegidos.
     * <li>Configura headers de segurança (HSTS, CSP, etc).
     * </ul>
     */
    @Bean("defaultSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   FiltroJwt filtroJwt) {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/api/usuarios/autenticar",
                                "/api/usuarios/autorizar",
                                "/api/usuarios/entrar")
                        .permitAll()
                        .requestMatchers("/actuator/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Habilita CSRF usando cookies (padrão para SPAs como Vue/React)
                // O cliente deve ler o cookie XSRF-TOKEN e enviar no header X-XSRF-TOKEN
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/usuarios/autenticar", "/api/usuarios/autorizar", "/api/usuarios/entrar")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'none'; frame-ancestors 'none'; sandbox"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(HeadersConfigurer.XXssConfig::disable))
                .addFilterAfter(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    FilterChain filterChain) throws ServletException, IOException {
                        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                        if (csrfToken != null) {
                            csrfToken.getToken();
                        }
                        filterChain.doFilter(request, response);
                    }
                }, CsrfFilter.class)
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
