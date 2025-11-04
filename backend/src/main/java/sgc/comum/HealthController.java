package sgc.comum;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// TODO Verificar se é usado mesmo. Senao, apagar.
public class HealthController {
    @GetMapping("/api/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("OK");
    }
}
