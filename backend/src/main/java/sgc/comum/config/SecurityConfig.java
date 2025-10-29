package sgc.comum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Adicionar esta importação

@Configuration
@EnableWebSecurity
@Profile("!test")
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; // Injetar JwtTokenProvider

    /**
     * Configura a cadeia de filtros de segurança para a aplicação.
     * <p>
     * Esta configuração define as seguintes regras:
     * <ul>
     *     <li>Permite acesso anônimo aos endpoints de autenticação.</li>
     *     <li>Exige autenticação para todos os outros endpoints sob '/api/'.</li>
     *     <li>Permite acesso a qualquer outra requisição (ex: frontend estático).</li>
     *     <li>Desabilita CSRF, HTTP Basic e formulário de login, adequando-se a uma API RESTful.</li>
     *     <li>Retorna status 401 Unauthorized para tentativas de acesso não autenticadas a endpoints protegidos.</li>
     * </ul>
     *
     * @param http o construtor {@link HttpSecurity} para configurar a segurança.
     * @return o {@link SecurityFilterChain} configurado.
     * @throws Exception se ocorrer um erro durante a configuração.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/usuarios/autenticar",
                    "/api/usuarios/autorizar",
                    "/api/usuarios/entrar",
                    "/api/test/**",
                    "/actuator/**"
                ).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173")); // Permitir o frontend
                config.setAllowedMethods(List.of("GET", "POST"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // Adicionar o filtro JWT
        return http.build();
    }
}
