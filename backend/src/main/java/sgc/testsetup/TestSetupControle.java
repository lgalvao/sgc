package sgc.testsetup;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test-setup")
@Profile("test") // Only active for 'test' Spring profile
@RequiredArgsConstructor
// TODO isso parece lixo!
public class TestSetupControle {
    private final TestSetupService testSetupService;

    @PostMapping("/{scenario}")
    public ResponseEntity<?> setupScenario(@PathVariable String scenario, @RequestBody(required = false) Map<String, Object> params) {
        return switch (scenario) {
            case "processoComMapaFinalizado" ->
                    ResponseEntity.ok(testSetupService.criarProcessoComMapaFinalizado(params));
            default -> ResponseEntity.badRequest().body("Unknown scenario: " + scenario);
        };
    }
}
