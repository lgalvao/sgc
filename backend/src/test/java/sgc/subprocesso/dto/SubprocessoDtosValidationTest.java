package sgc.subprocesso.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    @DisplayName("AceitarCadastroReq")
    class AceitarCadastroReqTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Cadastro aceito conforme revisão.", "Observação qualquer"})
        @DisplayName("Deve aceitar observações válidas (nulas, vazias ou preenchidas)")
        void deveAceitarObservacoesValidas(String observacao) {
            AceitarCadastroReq req = AceitarCadastroReq.builder()
                    .observacoes(observacao)
                    .build();

            Set<ConstraintViolation<AceitarCadastroReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar observações no limite máximo")
        void deveAceitarObservacoesNoLimite() {
            AceitarCadastroReq req = AceitarCadastroReq.builder()
                    .observacoes("a".repeat(500))
                    .build();

            Set<ConstraintViolation<AceitarCadastroReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar observações acima do limite")
        void deveRejeitarObservacoesAcimaDoLimite() {
            AceitarCadastroReq req = AceitarCadastroReq.builder()
                    .observacoes("a".repeat(501))
                    .build();

            Set<ConstraintViolation<AceitarCadastroReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("observacoes");
        }
    }

    @Nested
    @DisplayName("DevolverCadastroReq")
    class DevolverCadastroReqTests {

        @Test
        @DisplayName("Deve aceitar observações válidas")
        void deveAceitarObservacoesValidas() {
            DevolverCadastroReq req = DevolverCadastroReq.builder()
                    .observacoes("Cadastro devolvido para correções.")
                    .build();

            Set<ConstraintViolation<DevolverCadastroReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar observações nulas (campo opcional)")
        void deveAceitarObservacoesNulas() {
            DevolverCadastroReq req = DevolverCadastroReq.builder()
                    .observacoes(null)
                    .build();

            Set<ConstraintViolation<DevolverCadastroReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar observações acima do limite")
        void deveRejeitarObservacoesAcimaDoLimite() {
            DevolverCadastroReq req = DevolverCadastroReq.builder()
                    .observacoes("a".repeat(501))
                    .build();

            Set<ConstraintViolation<DevolverCadastroReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("observacoes");
        }
    }

    @Nested
    @DisplayName("HomologarCadastroReq")
    class HomologarCadastroReqTests {

        @Test
        @DisplayName("Deve aceitar observações válidas")
        void deveAceitarObservacoesValidas() {
            HomologarCadastroReq req = HomologarCadastroReq.builder()
                    .observacoes("Cadastro homologado.")
                    .build();

            Set<ConstraintViolation<HomologarCadastroReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar observações nulas (campo opcional)")
        void deveAceitarObservacoesNulas() {
            HomologarCadastroReq req = HomologarCadastroReq.builder()
                    .observacoes(null)
                    .build();

            Set<ConstraintViolation<HomologarCadastroReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar observações acima do limite")
        void deveRejeitarObservacoesAcimaDoLimite() {
            HomologarCadastroReq req = HomologarCadastroReq.builder()
                    .observacoes("a".repeat(501))
                    .build();

            Set<ConstraintViolation<HomologarCadastroReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("observacoes");
        }
    }

    @Nested
    @DisplayName("DevolverValidacaoReq")
    class DevolverValidacaoReqTests {

        @Test
        @DisplayName("Deve aceitar justificativa válida")
        void deveAceitarJustificativaValida() {
            DevolverValidacaoReq req = DevolverValidacaoReq.builder()
                    .justificativa("Validação devolvida por falta de competências.")
                    .build();

            Set<ConstraintViolation<DevolverValidacaoReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar justificativa nula")
        void deveRejeitarJustificativaNula() {
            DevolverValidacaoReq req = DevolverValidacaoReq.builder()
                    .justificativa(null)
                    .build();

            Set<ConstraintViolation<DevolverValidacaoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("justificativa");
        }

        @Test
        @DisplayName("Deve rejeitar justificativa em branco")
        void deveRejeitarJustificativaEmBranco() {
            DevolverValidacaoReq req = DevolverValidacaoReq.builder()
                    .justificativa("   ")
                    .build();

            Set<ConstraintViolation<DevolverValidacaoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("justificativa");
        }
    }
}
