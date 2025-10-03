package sgc.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sistema-acesso")
public class SistemaAcessoProperties {
    private String url;
    private String apiKey;
    private int maxRetries = 3;
    private long baseBackoffMs = 200L;
    private long maxBackoffMs = 2000L;
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 5000;
}