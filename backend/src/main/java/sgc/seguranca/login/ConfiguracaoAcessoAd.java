package sgc.seguranca.login;

import lombok.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.web.client.*;

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
                .baseUrl(properties.baseUrl())
                .build();
    }
}
