package sgc.configuracao.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("Parametro Validation")
class ParametroValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar parametro válido")
    void validarParametroValido() {
        Parametro p = new Parametro("CHAVE", "Descricao", "VALOR");
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve falhar quando chave é vazia")
    void validarChaveVazia() {
        Parametro p = new Parametro("", "Descricao", "VALOR");
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("A chave não pode estar vazia", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando valor é vazio")
    void validarValorVazio() {
        Parametro p = new Parametro("CHAVE", "Descricao", "");
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("O valor não pode estar vazio", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando chave é muito longa")
    void validarChaveLonga() {
        String chaveLonga = "A".repeat(51);
        Parametro p = new Parametro(chaveLonga, "Descricao", "VALOR");
        Set<ConstraintViolation<Parametro>> violations = validator.validate(p);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("A chave deve ter no máximo 50 caracteres", violations.iterator().next().getMessage());
    }
}
