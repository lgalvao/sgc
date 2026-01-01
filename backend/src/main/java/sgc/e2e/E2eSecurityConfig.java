package sgc.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import sgc.seguranca.FiltroAutenticacaoSimulado;

import java.util.List;

/**
 * Configuração de segurança específica para testes E2E.
 *
 * <p>Similar à configuração padrão, mas permite acesso aos endpoints /e2e/** sem autenticação.
 * Os métodos de fixture usam SecurityContext interno para chamar métodos com @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("e2e")
public class E2eSecurityConfig {
    private final FiltroAutenticacaoSimulado filtroAutenticacaoSimulado;

    /**
     * Configura a cadeia de filtros de segurança para testes E2E.
     *
     * <p>Permite acesso aos endpoints de fixture E2E sem autenticação, mas mantém
     * a autenticação para outros endpoints da API.
     *
     * @param http o construtor {@link HttpSecurity} para configurar a segurança.
     * @return o {@link SecurityFilterChain} configurado.
     * @throws Exception se ocorrer um erro durante a configuração.
     */
    @Bean
    public SecurityFilterChain e2eSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
                        auth.requestMatchers(
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
                .addFilterBefore(filtroAutenticacaoSimulado, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
