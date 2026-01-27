package sgc.comum.erros;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecurityController {
    @PostMapping("/test/validacao")
    void teste(@Valid @RequestBody TestDto dto) {
    }

    static class TestDto {
        @Size(min = 5)
        @JsonProperty("dadoSensivel")
        public String dadoSensivel;
    }
}
