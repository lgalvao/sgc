package sgc.autenticacao;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AcessoAdProperties.class)
@RequiredArgsConstructor
@Profile("!test & !e2e")
public class AcessoAdConfig {
    private final AcessoAdProperties properties;

    @Bean
    public RestClient acessoAdRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
