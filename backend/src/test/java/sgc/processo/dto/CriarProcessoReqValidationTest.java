package sgc.processo.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Validação: CriarProcessoRequest")
class CriarProcessoReqValidationTest {
    private Validator validator;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Requisições válidas")
    @SuppressWarnings("unused")
    class RequisicoesValidas {

        @Test
        @DisplayName("Deve aceitar requisição com todos os campos preenchidos corretamente")
        void deveAceitarRequisicaoCompleta() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Mapeamento 2026")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L, 2L, 3L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar requisição com uma única unidade")
        void deveAceitarRequisicaoComUmaUnidade() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Revisão 2026")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(15))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validação de descrição")
    @SuppressWarnings("unused")
    class ValidacaoDescricao {
        @Test
        @DisplayName("Deve rejeitar descrição nula")
        void deveRejeitarDescricaoNula() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao(null)
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("descricao");
        }

        @Test
        @DisplayName("Deve rejeitar descrição em branco")
        void deveRejeitarDescricaoEmBranco() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("   ")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("descricao");
        }

        @Test
        @DisplayName("Deve rejeitar descrição vazia")
        void deveRejeitarDescricaoVazia() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("descricao");
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("Validação de tipo")
    class ValidacaoTipo {

        @Test
        @DisplayName("Deve rejeitar tipo nulo")
        void deveRejeitarTipoNulo() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Processo Teste")
                    .tipo(null)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("tipo");
        }
    }

    @Nested
    @DisplayName("Validação de data limite")
    @SuppressWarnings("unused")
    class ValidacaoDataLimite {

        @Test
        @DisplayName("Deve rejeitar data limite nula")
        void deveRejeitarDataLimiteNula() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(null)
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("dataLimiteEtapa1");
        }

        @Test
        @DisplayName("Deve rejeitar data limite no passado")
        void deveRejeitarDataLimiteNoPassado() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().minusDays(1))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("dataLimiteEtapa1");
        }

        @Test
        @DisplayName("Deve rejeitar data limite igual a agora")
        void deveRejeitarDataLimiteIgualAgora() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now())
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("dataLimiteEtapa1");
        }
    }

    @Nested
    @DisplayName("Validação de unidades")
    @SuppressWarnings("unused")
    class ValidacaoUnidades {

        @Test
        @DisplayName("Deve rejeitar lista de unidades nula")
        void deveRejeitarUnidadesNulas() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(null)
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("unidades");
        }

        @Test
        @DisplayName("Deve rejeitar lista de unidades vazia")
        void deveRejeitarUnidadesVazias() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of())
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("unidades");
        }
    }

    @Nested
    @DisplayName("Validação de múltiplos campos inválidos")
        @SuppressWarnings("unused")
class ValidacaoMultipla {

        @Test
        @DisplayName("Deve reportar todas as violações quando múltiplos campos são inválidos")
        void deveReportarTodasViolacoes() {
            CriarProcessoRequest req = CriarProcessoRequest.builder()
                    .descricao(null)
                    .tipo(null)
                    .dataLimiteEtapa1(null)
                    .unidades(null)
                    .build();

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(4)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("descricao", "tipo", "dataLimiteEtapa1", "unidades");
        }
    }
}
