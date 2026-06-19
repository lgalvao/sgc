package sgc.e2e;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import sgc.comum.util.FiltroMonitoramentoHttp;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.seguranca.login.FiltroJwt;
import sgc.seguranca.login.GerenciadorJwt;
import sgc.seguranca.login.ListaNegraJwt;

import java.util.Arrays;
import java.util.List;

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
    private final List<String> origensPermitidas;

    public E2eSecurityConfig(
            @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:4173}") String origensPermitidasCsv
    ) {
        this.origensPermitidas = Arrays.stream(origensPermitidasCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

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
     * Configura a cadeia de filtros de segurança para testes E2E.
     *
     * <p>
     * Permite acesso aos endpoints de fixture E2E sem autenticação, mas mantém
     * a autenticação para outros endpoints da API.
     */
    @Bean
    public SecurityFilterChain e2eSecurityFilterChain(HttpSecurity http,
                                                      FiltroJwt filtroJwt,
                                                      FiltroMonitoramentoHttp filtroMonitoramentoHttp) {
        http.authorizeHttpRequests(auth ->         auth.dispatcherTypeMatchers(
                DispatcherType.ASYNC)
                        .permitAll()
                        .requestMatchers(
                "/api/usuarios/login",
                "/api/usuarios/entrar",
                "/api/usuarios/logout",
                "/actuator/**",
                "/e2e/**")                        .permitAll()
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
                    config.setAllowedOrigins(origensPermitidas);
                    config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .addFilterBefore(filtroMonitoramentoHttp, SecurityContextHolderFilter.class)
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
