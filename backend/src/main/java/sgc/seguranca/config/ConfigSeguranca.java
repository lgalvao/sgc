package sgc.seguranca.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import sgc.comum.util.FiltroMonitoramentoHttp;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.seguranca.login.FiltroJwt;
import sgc.seguranca.login.GerenciadorJwt;
import sgc.seguranca.login.ListaNegraJwt;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("(!test & !e2e) | secure-test")
public class ConfigSeguranca {

    @Bean
    public FiltroJwt filtroJwt(GerenciadorJwt gerenciadorJwt, UsuarioAplicacaoService usuarioAplicacaoService, ListaNegraJwt listaNegraJwt) {
        return new FiltroJwt(gerenciadorJwt, usuarioAplicacaoService, listaNegraJwt);
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
                                                   FiltroJwt filtroJwt,
                                                   FiltroMonitoramentoHttp filtroMonitoramentoHttp) {
        http.authorizeHttpRequests(auth ->         auth.dispatcherTypeMatchers(
                DispatcherType.ASYNC)
                        .permitAll()
                        .requestMatchers(
                "/api/usuarios/login",
                "/api/usuarios/entrar",
                "/api/usuarios/logout")                        .permitAll()
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
                        .ignoringRequestMatchers("/api/usuarios/login", "/api/usuarios/entrar", "/api/usuarios/logout")
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
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data: blob:; " +
                                                "font-src 'self' data:; " +
                                                "connect-src 'self'; " +
                                                "object-src 'none'; " +
                                                "base-uri 'self'; " +
                                                "frame-ancestors 'none'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(HeadersConfigurer.XXssConfig::disable))
                .addFilterAfter(new FiltroMaterializacaoCsrf(), CsrfFilter.class)
                .addFilterBefore(filtroMonitoramentoHttp, SecurityContextHolderFilter.class)
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    static class FiltroMaterializacaoCsrf extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
