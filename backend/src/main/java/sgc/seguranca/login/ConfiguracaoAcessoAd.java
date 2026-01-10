package sgc.seguranca.login;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

/**
 * Configuração do cliente REST para integração com o serviço AcessoAD.
 */
@Configuration
@EnableConfigurationProperties(PropriedadesAcessoAd.class)
@RequiredArgsConstructor
@Profile("!test & !e2e")
public class ConfiguracaoAcessoAd {
    private final PropriedadesAcessoAd properties;

    @Bean
    public RestClient acessoAdRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
