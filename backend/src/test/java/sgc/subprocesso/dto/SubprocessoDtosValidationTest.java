package sgc.subprocesso.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Validação: DTOs de Subprocesso")
class SubprocessoDtosValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("AceitarCadastroRequest")
    class AceitarCadastroReqTests {

        @ParameterizedTest
        @ValueSource(strings = {"Cadastro aceito conforme revisão.", "Observação qualquer"})
        @DisplayName("Deve aceitar observações válidas")
        void deveAceitarObservacoesValidas(String observacao) {
            AceitarCadastroRequest req = AceitarCadastroRequest.builder()
                    .observacoes(observacao)
                    .build();

            Set<ConstraintViolation<AceitarCadastroRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Não deve aceitar observações nulas")
        void naoDeveAceitarObservacoesNulas() {
            AceitarCadastroRequest req = AceitarCadastroRequest.builder().observacoes(null).build();
            Set<ConstraintViolation<AceitarCadastroRequest>> violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("Não deve aceitar observações vazias")
        void naoDeveAceitarObservacoesVazias() {
            AceitarCadastroRequest req = AceitarCadastroRequest.builder().observacoes("").build();
            Set<ConstraintViolation<AceitarCadastroRequest>> violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("Deve aceitar observações no limite máximo")
        void deveAceitarObservacoesNoLimite() {
            AceitarCadastroRequest req = AceitarCadastroRequest.builder()
                    .observacoes("a".repeat(500))
                    .build();

            Set<ConstraintViolation<AceitarCadastroRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar observações acima do limite")
        void deveRejeitarObservacoesAcimaDoLimite() {
            AceitarCadastroRequest req = AceitarCadastroRequest.builder()
                    .observacoes("a".repeat(501))
                    .build();

            Set<ConstraintViolation<AceitarCadastroRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("observacoes");
        }
    }

    @Nested
    @DisplayName("DevolverCadastroRequest")
    class DevolverCadastroReqTests {

        @Test
        @DisplayName("Deve aceitar observações válidas")
        void deveAceitarObservacoesValidas() {
            DevolverCadastroRequest req = DevolverCadastroRequest.builder()
                    .observacoes("Cadastro devolvido para correções.")
                    .build();

            Set<ConstraintViolation<DevolverCadastroRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Não deve aceitar observações nulas")
        void naoDeveAceitarObservacoesNulas() {
            DevolverCadastroRequest req = DevolverCadastroRequest.builder()
                    .observacoes(null)
                    .build();

            Set<ConstraintViolation<DevolverCadastroRequest>> violations = validator.validate(req);

            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar observações acima do limite")
        void deveRejeitarObservacoesAcimaDoLimite() {
            DevolverCadastroRequest req = DevolverCadastroRequest.builder()
                    .observacoes("a".repeat(501))
                    .build();

            Set<ConstraintViolation<DevolverCadastroRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("observacoes");
        }
    }

    @Nested
    @DisplayName("HomologarCadastroRequest")
    class HomologarCadastroReqTests {

        @Test
        @DisplayName("Deve aceitar observações válidas")
        void deveAceitarObservacoesValidas() {
            HomologarCadastroRequest req = HomologarCadastroRequest.builder()
                    .observacoes("Cadastro homologado.")
                    .build();

            Set<ConstraintViolation<HomologarCadastroRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar observações nulas (campo opcional)")
        void deveAceitarObservacoesNulas() {
            HomologarCadastroRequest req = HomologarCadastroRequest.builder()
                    .observacoes(null)
                    .build();

            Set<ConstraintViolation<HomologarCadastroRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar observações acima do limite")
        void deveRejeitarObservacoesAcimaDoLimite() {
            HomologarCadastroRequest req = HomologarCadastroRequest.builder()
                    .observacoes("a".repeat(501))
                    .build();

            Set<ConstraintViolation<HomologarCadastroRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("observacoes");
        }
    }

    @Nested
    @DisplayName("DevolverValidacaoRequest")
    class DevolverValidacaoReqTests {

        @Test
        @DisplayName("Deve aceitar justificativa válida")
        void deveAceitarJustificativaValida() {
            DevolverValidacaoRequest req = DevolverValidacaoRequest.builder()
                    .justificativa("Validação devolvida por falta de competências.")
                    .build();

            Set<ConstraintViolation<DevolverValidacaoRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar justificativa nula")
        void deveRejeitarJustificativaNula() {
            DevolverValidacaoRequest req = DevolverValidacaoRequest.builder()
                    .justificativa(null)
                    .build();

            Set<ConstraintViolation<DevolverValidacaoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("justificativa");
        }

        @Test
        @DisplayName("Deve rejeitar justificativa em branco")
        void deveRejeitarJustificativaEmBranco() {
            DevolverValidacaoRequest req = DevolverValidacaoRequest.builder()
                    .justificativa("   ")
                    .build();

            Set<ConstraintViolation<DevolverValidacaoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("justificativa");
        }
    }
}
