package sgc.comum.model;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("fornecerTitulosInvalidos")
    @DisplayName("Deve rejeitar títulos eleitorais inválidos")
    void deveRejeitarTitulosEleitoraisInvalidos(String titulo, String mensagemEsperada) {
        RequisicaoTeste requisicao = new RequisicaoTeste(titulo);

        Set<ConstraintViolation<RequisicaoTeste>> violations = validator.validate(requisicao);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly(mensagemEsperada);
    }

    private static Stream<Arguments> fornecerTitulosInvalidos() {
        return Stream.of(
                Arguments.of(null, "O título eleitoral é obrigatório."),
                Arguments.of("1234567890123", "O título eleitoral deve ter no máximo 12 caracteres."),
                Arguments.of("1234ABCD", "O título eleitoral deve conter apenas números.")
        );
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
