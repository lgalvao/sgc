package sgc.configuracoes.model;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import sgc.comum.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Configuracao validation")
class ConfiguracaoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    @DisplayName("Deve validar parametro válido")
    void validarParametroValido() {
        Configuracao p = Configuracao.builder().chave("CHAVE").descricao("Descricao").valor("VALOR").build();
        Set<ConstraintViolation<Configuracao>> violations = validator.validate(p);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve falhar quando chave é vazia")
    void validarChaveVazia() {
        Configuracao p = Configuracao.builder().chave("").descricao("Descricao").valor("VALOR").build();
        Set<ConstraintViolation<Configuracao>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(Mensagens.CHAVE_OBRIGATORIA);
    }

    @Test
    @DisplayName("Deve falhar quando valor é vazio")
    void validarValorVazio() {
        Configuracao p = Configuracao.builder().chave("CHAVE").descricao("Descricao").valor("").build();
        Set<ConstraintViolation<Configuracao>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(Mensagens.VALOR_OBRIGATORIO);
    }

    @Test
    @DisplayName("Deve falhar quando chave é muito longa")
    void validarChaveLonga() {
        String chaveLonga = "A".repeat(51);
        Configuracao p = Configuracao.builder().chave(chaveLonga).descricao("Descricao").valor("VALOR").build();
        Set<ConstraintViolation<Configuracao>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(Mensagens.CHAVE_MAX);
    }
}