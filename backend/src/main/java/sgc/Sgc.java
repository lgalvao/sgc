package sgc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
@EnableAsync  // Habilita processamento assíncrono de eventos
public class Sgc {
    public static void main(String[] args) {
        SpringApplication.run(Sgc.class, args);
    }
}
