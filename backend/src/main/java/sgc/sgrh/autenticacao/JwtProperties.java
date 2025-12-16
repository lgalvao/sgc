package sgc.sgrh.autenticacao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aplicacao.jwt")
@Component
@Data
public class JwtProperties {
    private String secret = "sgc-secret-key-change-this-in-production-minimum-32-chars";
    private int expiracaoMinutos = 120;
}
