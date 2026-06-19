package sgc.comum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import sgc.seguranca.config.ConfigCorsProperties;
import sgc.seguranca.config.JwtProperties;
import sgc.seguranca.login.PropriedadesAcessoAd;

/**
 * Configuração centralizada para a aplicação SGC sob o prefixo 'aplicacao'.
 * Unifica e documenta todas as propriedades customizadas para suporte do IDE.
 */
@Setter
@Getter
@Component
@ConfigurationProperties("aplicacao")
@SuppressWarnings("NullAway.Init")
public class ConfigAplicacao {
    @NestedConfigurationProperty
    private final OpenApi openapi = new OpenApi();
    @NestedConfigurationProperty
    private final Email email = new Email();
    private boolean ambienteTestes;
    private String urlAcessoHom;
    private String urlAcessoProd;
    private String urlLeitorEmailTestes;
    @NestedConfigurationProperty
    private JwtProperties jwt;

    @NestedConfigurationProperty
    private ConfigCorsProperties cors;

    @NestedConfigurationProperty
    private PropriedadesAcessoAd acessoAd;

    @Getter
    @Setter
    @SuppressWarnings("NullAway.Init")
    public static class OpenApi {
        private String title;
        private String version;
        private String description;
    }

    @Getter
    @Setter
    @SuppressWarnings("NullAway.Init")
    public static class Email {
        private String remetente;
        private String remetenteNome;
        private String assuntoPrefixo;
    }
}
