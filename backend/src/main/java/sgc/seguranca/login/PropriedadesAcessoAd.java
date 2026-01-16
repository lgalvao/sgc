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
) {
    private static final String DEFAULT_BASE_URL = "https://sedesenvdev01.tre-pe.gov.br/acessoAD";
    private static final String DEFAULT_CODIGO_SISTEMA = "SGC";

    public PropriedadesAcessoAd {
        baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        codigoSistema = codigoSistema != null ? codigoSistema : DEFAULT_CODIGO_SISTEMA;
    }
}
