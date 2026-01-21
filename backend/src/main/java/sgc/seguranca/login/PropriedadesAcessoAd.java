package sgc.seguranca.login;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para integração com o serviço AcessoAD.
 *
 * <p>Record imutável com valores default via compact constructor.
 */
@ConfigurationProperties(prefix = "aplicacao.acesso-ad")
public record PropriedadesAcessoAd(
    String baseUrl,
    String codigoSistema
) {}
