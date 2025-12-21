package sgc.sgrh.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes de Validação: AutenticacaoReq")
class AutenticacaoReqValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar inputs normais")
    void deveAceitarInputsNormais() {
        // Arrange
        AutenticacaoReq req = AutenticacaoReq.builder()
                .tituloEleitoral("123456789012")
                .senha("senha123")
                .build();

        // Act
        Set<ConstraintViolation<AutenticacaoReq>> violations = validator.validate(req);

        // Assert
        assertTrue(violations.isEmpty(), "Deveria passar na validação com inputs normais");
    }

    @Test
    @DisplayName("Deve rejeitar inputs excessivamente longos")
    void deveRejeitarInputsExcessivamenteLongos() {
        // Arrange
        String tituloLongo = "a".repeat(1000);
        String senhaLonga = "b".repeat(5000);

        AutenticacaoReq req = AutenticacaoReq.builder()
                .tituloEleitoral(tituloLongo)
                .senha(senhaLonga)
                .build();

        // Act
        Set<ConstraintViolation<AutenticacaoReq>> violations = validator.validate(req);

        // Assert
        assertFalse(violations.isEmpty(), "Inputs longos devem ser rejeitados por segurança");
    }
}
