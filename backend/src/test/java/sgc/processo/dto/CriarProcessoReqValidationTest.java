package sgc.processo.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Validação: CriarProcessoReq")
class CriarProcessoReqValidationTest {

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
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Mapeamento 2026")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L, 2L, 3L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Deve aceitar requisição com uma única unidade")
        void deveAceitarRequisicaoComUmaUnidade() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Revisão 2026")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(15))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validação de descrição")
    class ValidacaoDescricao {

        @Test
        @DisplayName("Deve rejeitar descrição nula")
        void deveRejeitarDescricaoNula() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao(null)
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("descricao");
        }

        @Test
        @DisplayName("Deve rejeitar descrição em branco")
        void deveRejeitarDescricaoEmBranco() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("   ")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("descricao");
        }

        @Test
        @DisplayName("Deve rejeitar descrição vazia")
        void deveRejeitarDescricaoVazia() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("descricao");
        }
    }

    @Nested
    @DisplayName("Validação de tipo")
    class ValidacaoTipo {

        @Test
        @DisplayName("Deve rejeitar tipo nulo")
        void deveRejeitarTipoNulo() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Processo Teste")
                    .tipo(null)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("tipo");
        }
    }

    @Nested
    @DisplayName("Validação de data limite")
    class ValidacaoDataLimite {

        @Test
        @DisplayName("Deve rejeitar data limite nula")
        void deveRejeitarDataLimiteNula() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(null)
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("dataLimiteEtapa1");
        }

        @Test
        @DisplayName("Deve rejeitar data limite no passado")
        void deveRejeitarDataLimiteNoPassado() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().minusDays(1))
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("dataLimiteEtapa1");
        }

        @Test
        @DisplayName("Deve rejeitar data limite igual a agora")
        void deveRejeitarDataLimiteIgualAgora() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now())
                    .unidades(List.of(1L))
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("dataLimiteEtapa1");
        }
    }

    @Nested
    @DisplayName("Validação de unidades")
    class ValidacaoUnidades {

        @Test
        @DisplayName("Deve rejeitar lista de unidades nula")
        void deveRejeitarUnidadesNulas() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(null)
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("unidades");
        }

        @Test
        @DisplayName("Deve rejeitar lista de unidades vazia")
        void deveRejeitarUnidadesVazias() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Processo Teste")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of())
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("unidades");
        }
    }

    @Nested
    @DisplayName("Validação de múltiplos campos inválidos")
    class ValidacaoMultipla {

        @Test
        @DisplayName("Deve reportar todas as violações quando múltiplos campos são inválidos")
        void deveReportarTodasViolacoes() {
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao(null)
                    .tipo(null)
                    .dataLimiteEtapa1(null)
                    .unidades(null)
                    .build();

            Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(4)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("descricao", "tipo", "dataLimiteEtapa1", "unidades");
        }
    }
}
