package sgc.comum.config;

import lombok.*;
import org.springframework.boot.context.properties.*;
import org.springframework.stereotype.*;
import sgc.seguranca.config.*;
import sgc.seguranca.login.*;

/**
 * Configuração centralizada para a aplicação SGC sob o prefixo 'aplicacao'.
 * Unifica e documenta todas as propriedades customizadas para suporte do IDE.
 */
@Setter
@Getter
@Component
@ConfigurationProperties("aplicacao")
public class ConfigAplicacao {
    @NestedConfigurationProperty
    private final OpenApi openapi = new OpenApi();
    @NestedConfigurationProperty
    private final Email email = new Email();
    private boolean ambienteTestes;
    private String urlAcessoHom;
    private String urlAcessoProd;
    @NestedConfigurationProperty
    private JwtProperties jwt;

    @NestedConfigurationProperty
    private ConfigCorsProperties cors;

    @NestedConfigurationProperty
    private PropriedadesAcessoAd acessoAd;

    @Getter
    @Setter
    public static class OpenApi {
        private String title;
        private String version;
        private String description;
    }

    @Getter
    @Setter
    public static class Email {
        private String remetente;
        private String remetenteNome;
        private String assuntoPrefixo;
    }
}
