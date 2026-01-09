package sgc.seguranca.login;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para integração com o serviço AcessoAD.
 */
@ConfigurationProperties(prefix = "aplicacao.acesso-ad")
@Data
public class PropriedadesAcessoAd {
    private String baseUrl = "https://sedesenvdev01.tre-pe.gov.br/acessoAD";
    private String codigoSistema = "SGC";
}
