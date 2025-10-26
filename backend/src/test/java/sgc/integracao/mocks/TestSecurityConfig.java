package sgc.integracao.mocks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import sgc.sgrh.UsuarioRepo;
import sgc.unidade.modelo.UnidadeRepo;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

    @Bean
    @Profile({"test", "jules"})
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/usuarios/autenticar",
                    "/api/usuarios/autorizar",
                    "/api/usuarios/entrar",
                    "/api/test/**"
                ).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
