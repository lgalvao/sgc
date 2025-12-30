package sgc.seguranca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Teste de Verificação de Segurança do JWT")
class GerenciadorJwtSecurityTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(GerenciadorJwt.class)
            .withBean(JwtProperties.class, () -> {
                JwtProperties props = new JwtProperties();
                props.setSecret("sgc-secret-key-change-this-in-production-minimum-32-chars"); // Default secret
                return props;
            });

    @Test
    @DisplayName("Deve falhar ao iniciar em produção com secret padrão")
    void deveFalharEmProducaoComSecretPadrao() {
        contextRunner
                .withPropertyValues("spring.profiles.active=production")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class);
                    assertThat(context.getStartupFailure().getCause())
                            .hasMessageContaining("FALHA DE SEGURANÇA");
                });
    }

    @Test
    @DisplayName("Deve iniciar com sucesso em teste com secret padrão")
    void deveSucessoEmTesteComSecretPadrao() {
        contextRunner
                .withPropertyValues("spring.profiles.active=test")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("Deve iniciar com sucesso em e2e com secret padrão")
    void deveSucessoEmE2eComSecretPadrao() {
        contextRunner
                .withPropertyValues("spring.profiles.active=e2e")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("Deve iniciar com sucesso em local com secret padrão")
    void deveSucessoEmLocalComSecretPadrao() {
        contextRunner
                .withPropertyValues("spring.profiles.active=local")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("Deve iniciar com sucesso em produção se o secret for seguro")
    void deveSucessoEmProducaoComSecretSeguro() {
        new ApplicationContextRunner()
                .withUserConfiguration(GerenciadorJwt.class)
                .withBean(JwtProperties.class, () -> {
                    JwtProperties props = new JwtProperties();
                    props.setSecret("uma-senha-muito-segura-e-diferente-da-padrao-123456");
                    return props;
                })
                .withPropertyValues("spring.profiles.active=production")
                .run(context -> assertThat(context).hasNotFailed());
    }
}
