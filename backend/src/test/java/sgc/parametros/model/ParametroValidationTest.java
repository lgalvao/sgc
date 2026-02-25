package sgc.parametros.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Parametro Validation")
class ParametroValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    @DisplayName("Deve validar parametro válido")
    void validarParametroValido() {
        Parametro p = Parametro.builder().chave("CHAVE").descricao("Descricao").valor("VALOR").build();
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve falhar quando chave é vazia")
    void validarChaveVazia() {
        Parametro p = Parametro.builder().chave("").descricao("Descricao").valor("VALOR").build();
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("A chave não pode estar vazia");
    }

    @Test
    @DisplayName("Deve falhar quando valor é vazio")
    void validarValorVazio() {
        Parametro p = Parametro.builder().chave("CHAVE").descricao("Descricao").valor("").build();
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("O valor não pode estar vazio");
    }

    @Test
    @DisplayName("Deve falhar quando chave é muito longa")
    void validarChaveLonga() {
        String chaveLonga = "A".repeat(51);
        Parametro p = Parametro.builder().chave(chaveLonga).descricao("Descricao").valor("VALOR").build();
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("A chave deve ter no máximo 50 caracteres");
    }
}