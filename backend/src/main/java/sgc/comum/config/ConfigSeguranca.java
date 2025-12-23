package sgc.comum.config;

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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("(!test & !e2e) | secure-test")
public class ConfigSeguranca {
    private final FiltroAutenticacaoSimulado filtroAutenticacaoSimulado;

    /**
     * Configura a cadeia de filtros de segurança para a aplicação.
     *
     * <p>Esta configuração define as seguintes regras:
     *
     * <ul>
     *   <li>Permite acesso anônimo aos endpoints de autenticação.
     *   <li>Exige autenticação para todos os outros endpoints sob '/api/'.
     *   <li>Permite acesso a qualquer outra requisição (ex: frontend estático).
     *   <li>Desabilita CSRF, HTTP Basic e formulário de login, adequando-se a uma API RESTful.
     *   <li>Retorna status 401 Unauthorized para tentativas de acesso não autenticadas a endpoints
     *       protegidos.
     * </ul>
     *
     * @param http o construtor {@link HttpSecurity} para configurar a segurança.
     * @return o {@link SecurityFilterChain} configurado.
     */
    @Bean("defaultSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedMethods(List.of("GET", "POST"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .addFilterBefore(filtroAutenticacaoSimulado, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
