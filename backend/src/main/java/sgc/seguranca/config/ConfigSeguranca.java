package sgc.seguranca.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import sgc.organizacao.UsuarioFacade;
import sgc.seguranca.login.FiltroJwt;
import sgc.seguranca.login.GerenciadorJwt;

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
     *
     * @param http o construtor {@link HttpSecurity} para configurar a segurança.
     * @return o {@link SecurityFilterChain} configurado.
     */
    @Bean("defaultSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   FiltroJwt filtroJwt) throws Exception {
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
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
