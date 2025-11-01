package sgc.testsetup;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/test-setup")
@Profile("test") // Only active for 'test' Spring profile
@RequiredArgsConstructor
public class TestSetupControle {

    private final TestSetupService testSetupService;

    @PostMapping("/{scenario}")
    public ResponseEntity<?> setupScenario(@PathVariable String scenario, @RequestBody(required = false) Map<String, Object> params) {
        switch (scenario) {
            case "processoComMapaFinalizado":
                return ResponseEntity.ok(testSetupService.criarProcessoComMapaFinalizado(params));
            default:
                return ResponseEntity.badRequest().body("Unknown scenario: " + scenario);
        }
    }
}
