package sgc.seguranca.login.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Validação: EntrarReq")
class EntrarReqValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Requisições válidas")
    class RequisicoesValidas {

        @Test
        @DisplayName("Deve aceitar requisição com todos os campos preenchidos corretamente")
        void deveAceitarRequisicaoCompleta() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral("123456789012")
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar diferentes perfis válidos")
        void deveAceitarDiferentesPerfis() {
            String[] perfisValidos = {"ADMIN", "GESTOR", "CHEFE", "SERVIDOR"};
            
            for (String perfil : perfisValidos) {
                EntrarReq req = EntrarReq.builder()
                        .tituloEleitoral("123456789012")
                        .perfil(perfil)
                        .unidadeCodigo(1L)
                        .build();

                Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

                assertThat(violations)
                        .as("Perfil %s deve ser válido", perfil)
                        .isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Validação de título eleitoral")
    class ValidacaoTituloEleitoral {

        @Test
        @DisplayName("Deve rejeitar título eleitoral nulo")
        void deveRejeitarTituloNulo() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral(null)
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("tituloEleitoral");
        }

        @Test
        @DisplayName("Deve rejeitar título eleitoral muito longo")
        void deveRejeitarTituloMuitoLongo() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral("a".repeat(21))  // > 20 caracteres
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("tituloEleitoral");
        }

        @Test
        @DisplayName("Deve aceitar título eleitoral no limite máximo")
        void deveAceitarTituloNoLimite() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral("a".repeat(20))  // exatamente 20 caracteres
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validação de perfil")
    class ValidacaoPerfil {

        @Test
        @DisplayName("Deve rejeitar perfil nulo")
        void deveRejeitarPerfilNulo() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral("123456789012")
                    .perfil(null)
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("perfil");
        }

        @Test
        @DisplayName("Deve rejeitar perfil muito longo")
        void deveRejeitarPerfilMuitoLongo() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral("123456789012")
                    .perfil("a".repeat(51))  // > 50 caracteres
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("perfil");
        }
    }

    @Nested
    @DisplayName("Validação de unidade")
    class ValidacaoUnidade {

        @Test
        @DisplayName("Deve rejeitar código de unidade nulo")
        void deveRejeitarUnidadeNula() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral("123456789012")
                    .perfil("ADMIN")
                    .unidadeCodigo(null)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("unidadeCodigo");
        }
    }

    @Nested
    @DisplayName("Validação de múltiplos campos inválidos")
    class ValidacaoMultipla {

        @Test
        @DisplayName("Deve reportar todas as violações quando múltiplos campos são inválidos")
        void deveReportarTodasViolacoes() {
            EntrarReq req = EntrarReq.builder()
                    .tituloEleitoral(null)
                    .perfil(null)
                    .unidadeCodigo(null)
                    .build();

            Set<ConstraintViolation<EntrarReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(3)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("tituloEleitoral", "perfil", "unidadeCodigo");
        }
    }
}
