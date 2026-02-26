package sgc.seguranca.login.dto;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import sgc.seguranca.dto.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes de Validação: AutenticarRequest")
class AutenticarReqValidationTest {

    private ValidatorFactory factory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterEach
    void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    @DisplayName("Deve aceitar inputs normais")
    void deveAceitarInputsNormais() {
        // Arrange
        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral("123456789012")
                .senha("senha123")
                .build();

        // Act
        Set<ConstraintViolation<AutenticarRequest>> violations = validator.validate(req);

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

        AutenticarRequest req = AutenticarRequest.builder()
                .tituloEleitoral(tituloLongo)
                .senha(senhaLonga)
                .build();

        // Act
        Set<ConstraintViolation<AutenticarRequest>> violations = validator.validate(req);

        // Assert
        assertThat(violations).isNotEmpty();
    }
}
