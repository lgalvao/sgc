package sgc;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.properties.*;
import org.springframework.scheduling.annotation.*;

@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
public class Sgc {
    public static void main(String[] args) {
        SpringApplication.run(Sgc.class, args);
    }
}
