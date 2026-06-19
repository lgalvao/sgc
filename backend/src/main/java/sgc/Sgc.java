package sgc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SuppressWarnings("UnnecessaryModifier")
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
@EnableConfigurationProperties(sgc.comum.util.MonitoramentoProperties.class)
@SpringBootApplication(excludeName = {
        "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration"
})
public class Sgc {
    public static void main(String[] args) {
        SpringApplication.run(Sgc.class, args);
    }
}
