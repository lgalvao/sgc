package sgc.sgrh.internal.autenticacao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aplicacao.acesso-ad")
@Data
public class AcessoAdProperties {
    private String baseUrl = "https://sedesenvdev01.tre-pe.gov.br/acessoAD";
    private String codigoSistema = "SGC";
}
