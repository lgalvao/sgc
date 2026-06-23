package sgc.processo.dto;

import com.fasterxml.jackson.databind.*;
import jakarta.validation.*;
import org.junit.jupiter.api.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes de Validação: CriarProcessoRequest")
class CriarProcessoRequestValidationTest {
    private ValidatorFactory factory;
    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @AfterEach
    void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    private CriarProcessoRequest desserializarRequisicao(String json) {
        try {
            return objectMapper.readValue(json, CriarProcessoRequest.class);
        } catch (Exception e) {
            throw new AssertionError("Falha ao desserializar requisição de teste", e);
        }
    }

    @Nested
    @DisplayName("Requisições válidas")
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
    class ValidacaoDescricao {
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
    @DisplayName("Validação de data limite")
    class ValidacaoDataLimite {

        @Test
        @DisplayName("Deve rejeitar data limite nula")
        void deveRejeitarDataLimiteNula() {
            CriarProcessoRequest req = desserializarRequisicao("""
                    {
                      "descricao": "Processo teste",
                      "tipo": "MAPEAMENTO",
                      "dataLimiteEtapa1": null,
                      "unidades": [1]
                    }
                    """);

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
                    .descricao("Processo teste")
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
                    .descricao("Processo teste")
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
    class ValidacaoUnidades {
        @Test
        @DisplayName("Deve rejeitar lista de unidades nula")
        void deveRejeitarUnidadesNulas() {
            CriarProcessoRequest req = desserializarRequisicao("""
                    {
                      "descricao": "Processo teste",
                      "tipo": "MAPEAMENTO",
                      "dataLimiteEtapa1": "2030-12-31T10:00:00",
                      "unidades": null
                    }
                    """);

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
                    .descricao("Processo teste")
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
    class ValidacaoMultipla {
        @Test
        @DisplayName("Deve reportar todas as violações quando múltiplos campos são inválidos")
        void deveReportarTodasViolacoes() {
            CriarProcessoRequest req = desserializarRequisicao("""
                    {
                      "descricao": null,
                      "tipo": null,
                      "dataLimiteEtapa1": null,
                      "unidades": null
                    }
                    """);

            Set<ConstraintViolation<CriarProcessoRequest>> violations = validator.validate(req);

            assertThat(violations)
                    .hasSize(4)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("descricao", "tipo", "dataLimiteEtapa1", "unidades");
        }
    }
}
