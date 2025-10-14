package sgc.comum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("aplicacao")
public class ConfigAplicacao {
    private boolean ambienteTestes;
    private String urlAcessoHom;
    private String urlAcessoProd;
}
