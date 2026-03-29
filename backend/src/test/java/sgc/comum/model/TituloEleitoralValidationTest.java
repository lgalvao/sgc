package sgc.comum.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de Validação: TituloEleitoral")
class TituloEleitoralValidationTest {

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
    @DisplayName("Deve aceitar título eleitoral com 12 dígitos")
    void deveAceitarTituloEleitoralValido() {
        RequisicaoTeste requisicao = new RequisicaoTeste("123456789012");

        Set<ConstraintViolation<RequisicaoTeste>> violations = validator.validate(requisicao);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar título eleitoral nulo")
    void deveRejeitarTituloEleitoralNulo() {
        RequisicaoTeste requisicao = new RequisicaoTeste(null);

        Set<ConstraintViolation<RequisicaoTeste>> violations = validator.validate(requisicao);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("O título eleitoral é obrigatório.");
    }

    @Test
    @DisplayName("Deve rejeitar título eleitoral com mais de 12 caracteres")
    void deveRejeitarTituloEleitoralComMaisDeDozeCaracteres() {
        RequisicaoTeste requisicao = new RequisicaoTeste("1234567890123");

        Set<ConstraintViolation<RequisicaoTeste>> violations = validator.validate(requisicao);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("O título eleitoral deve ter no máximo 12 caracteres.");
    }

    @Test
    @DisplayName("Deve rejeitar título eleitoral com caracteres não numéricos")
    void deveRejeitarTituloEleitoralComCaracteresNaoNumericos() {
        RequisicaoTeste requisicao = new RequisicaoTeste("1234ABCD");

        Set<ConstraintViolation<RequisicaoTeste>> violations = validator.validate(requisicao);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("O título eleitoral deve conter apenas números.");
    }

    @Test
    @DisplayName("Deve manter metadados da anotação")
    void deveManterMetadadosDaAnotacao() {
        Target target = TituloEleitoral.class.getAnnotation(Target.class);
        Retention retention = TituloEleitoral.class.getAnnotation(Retention.class);

        assertThat(target).isNotNull();
        assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD,
                ElementType.PARAMETER,
                ElementType.RECORD_COMPONENT
        );
        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(java.lang.annotation.RetentionPolicy.RUNTIME);
        assertThat(TituloEleitoral.class.getDeclaredMethods())
                .extracting(Method::getName)
                .contains("message", "groups", "payload");
    }

    private record RequisicaoTeste(@TituloEleitoral String tituloEleitoral) {
    }
}
