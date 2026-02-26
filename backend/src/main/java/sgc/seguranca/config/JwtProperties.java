package sgc.seguranca.config;

import org.springframework.boot.context.properties.*;

/**
 * Propriedades de configuração JWT.
 *
 * <p>Record imutável com valor default para expiração.
 */
@ConfigurationProperties(prefix = "aplicacao.jwt")
public record JwtProperties(
        String secret,
        int expiracaoMinutos
) {
    private static final int EXPIRACAO_PADRAO = 120;

    public JwtProperties {
        expiracaoMinutos = expiracaoMinutos > 0 ? expiracaoMinutos : EXPIRACAO_PADRAO;
    }
}
