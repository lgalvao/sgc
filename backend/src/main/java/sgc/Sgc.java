package sgc;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.properties.*;
import org.springframework.scheduling.annotation.*;

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
