package sgc.seguranca.login.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Validação: AutenticarReq")
class AutenticarReqValidationTest {

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
        AutenticarReq req = AutenticarReq.builder()
                .tituloEleitoral("123456789012")
                .senha("senha123")
                .build();

        // Act
        Set<ConstraintViolation<AutenticarReq>> violations = validator.validate(req);

        // Assert
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "1000, 5000",
            "500, 1000",
            "2000, 3000"
    })
    @DisplayName("Deve rejeitar inputs excessivamente longos")
    void deveRejeitarInputsExcessivamenteLongos(int tamanhoTitulo, int tamanhoSenha) {
        // Arrange
        String tituloLongo = "a".repeat(tamanhoTitulo);
        String senhaLonga = "b".repeat(tamanhoSenha);

        AutenticarReq req = AutenticarReq.builder()
                .tituloEleitoral(tituloLongo)
                .senha(senhaLonga)
                .build();

        // Act
        Set<ConstraintViolation<AutenticarReq>> violations = validator.validate(req);

        // Assert
        assertThat(violations).isNotEmpty();
    }
}
