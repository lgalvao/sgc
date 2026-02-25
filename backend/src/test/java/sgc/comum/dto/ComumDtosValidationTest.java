package sgc.comum.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import sgc.comum.ComumDtos.DataRequest;
import sgc.comum.ComumDtos.JustificativaRequest;
import sgc.comum.ComumDtos.TextoRequest;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de Validação: ComumDtos")
class ComumDtosValidationTest {

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

    @Nested
    @DisplayName("TextoRequest")
    class TextoRequestTests {
        @Test
        @DisplayName("Deve aceitar texto válido")
        void deveAceitarTextoValido() {
            TextoRequest req = new TextoRequest("Texto válido");
            Set<ConstraintViolation<TextoRequest>> violations = validator.validate(req);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar texto em branco")
        void deveRejeitarTextoEmBranco() {
            TextoRequest req = new TextoRequest("   ");
            Set<ConstraintViolation<TextoRequest>> violations = validator.validate(req);
            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    @DisplayName("JustificativaRequest")
    class JustificativaRequestTests {
        @Test
        @DisplayName("Deve aceitar justificativa válida")
        void deveAceitarJustificativaValida() {
            JustificativaRequest req = new JustificativaRequest("Justificativa válida");
            Set<ConstraintViolation<JustificativaRequest>> violations = validator.validate(req);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar justificativa em branco")
        void deveRejeitarJustificativaEmBranco() {
            JustificativaRequest req = new JustificativaRequest("");
            Set<ConstraintViolation<JustificativaRequest>> violations = validator.validate(req);
            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    @DisplayName("DataRequest")
    class DataRequestTests {
        @Test
        @DisplayName("Deve aceitar data válida")
        void deveAceitarDataValida() {
            DataRequest req = new DataRequest(LocalDate.now());
            Set<ConstraintViolation<DataRequest>> violations = validator.validate(req);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar data nula")
        void deveRejeitarDataNula() {
            DataRequest req = new DataRequest(null);
            Set<ConstraintViolation<DataRequest>> violations = validator.validate(req);
            assertThat(violations).hasSize(1);
        }
    }
}
