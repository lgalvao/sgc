package sgc.sgrh.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutenticacaoReqValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void deveAceitarInputsNormais() {
        AutenticacaoReq req = AutenticacaoReq.builder()
                .tituloEleitoral("123456789012")
                .senha("senha123")
                .build();

        Set<ConstraintViolation<AutenticacaoReq>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Deveria passar na validação com inputs normais");
    }

    @Test
    void deveRejeitarInputsExcessivamenteLongos() {
        // Simulando um ataque DoS com payload grande
        String tituloLongo = "a".repeat(1000);
        String senhaLonga = "b".repeat(5000);

        AutenticacaoReq req = AutenticacaoReq.builder()
                .tituloEleitoral(tituloLongo)
                .senha(senhaLonga)
                .build();

        Set<ConstraintViolation<AutenticacaoReq>> violations = validator.validate(req);

        // Agora deve falhar devido ao @Size
        assertFalse(violations.isEmpty(), "Inputs longos devem ser rejeitados por segurança");
    }
}
