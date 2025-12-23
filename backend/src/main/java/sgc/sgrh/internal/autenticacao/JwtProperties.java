package sgc.sgrh.internal.autenticacao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aplicacao.jwt")
@Component
@Data
public class JwtProperties {
    private String secret;
    private int expiracaoMinutos = 120;
}
