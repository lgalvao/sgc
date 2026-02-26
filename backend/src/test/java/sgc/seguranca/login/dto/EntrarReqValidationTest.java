package sgc.seguranca.login.dto;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import sgc.seguranca.dto.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes de Validação: EntrarRequest")
class EntrarReqValidationTest {

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
    @DisplayName("Requisições válidas")
    class RequisicoesValidas {

        @Test
        @DisplayName("Deve aceitar requisição com todos os campos preenchidos corretamente")
        void deveAceitarRequisicaoCompleta() {
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral("123456789012")
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar diferentes perfis válidos")
        void deveAceitarDiferentesPerfis() {
            String[] perfisValidos = {"ADMIN", "GESTOR", "CHEFE", "SERVIDOR"};

            for (String perfil : perfisValidos) {
                EntrarRequest req = EntrarRequest.builder()
                        .tituloEleitoral("123456789012")
                        .perfil(perfil)
                        .unidadeCodigo(1L)
                        .build();

                Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

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
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral(null)
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("tituloEleitoral");
        }

        @Test
        @DisplayName("Deve rejeitar título eleitoral muito longo")
        void deveRejeitarTituloMuitoLongo() {
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral("1".repeat(13))  // > 12 caracteres
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("tituloEleitoral");
        }

        @Test
        @DisplayName("Deve aceitar título eleitoral no limite máximo")
        void deveAceitarTituloNoLimite() {
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral("1".repeat(12))  // exatamente 12 caracteres
                    .perfil("ADMIN")
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validação de perfil")
    class ValidacaoPerfil {

        @Test
        @DisplayName("Deve rejeitar perfil nulo")
        void deveRejeitarPerfilNulo() {
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral("123456789012")
                    .perfil(null)
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("perfil");
        }

        @Test
        @DisplayName("Deve rejeitar perfil muito longo")
        void deveRejeitarPerfilMuitoLongo() {
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral("123456789012")
                    .perfil("a".repeat(51))  // > 50 caracteres
                    .unidadeCodigo(1L)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

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
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral("123456789012")
                    .perfil("ADMIN")
                    .unidadeCodigo(null)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

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
            EntrarRequest req = EntrarRequest.builder()
                    .tituloEleitoral(null)
                    .perfil(null)
                    .unidadeCodigo(null)
                    .build();

            Set<ConstraintViolation<EntrarRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(3)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("tituloEleitoral", "perfil", "unidadeCodigo");
        }
    }
}
