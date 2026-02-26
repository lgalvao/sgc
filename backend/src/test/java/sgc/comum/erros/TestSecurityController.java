package sgc.comum.erros;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestSecurityController {
    @SuppressWarnings("EmptyMethod")
    @PostMapping("/test/validacao")
    void teste(@Valid @RequestBody TestDto dto) {
        // Implementação vazia pois a validação ocorre antes da execução do método
    }

    static class TestDto {
        @Size(min = 5)
        @JsonProperty("dadoSensivel")
        public String dadoSensivel;
    }
}
