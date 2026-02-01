package sgc.seguranca;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.dto.ApresentarSugestoesRequest;
import sgc.subprocesso.dto.DevolverValidacaoRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class InputValidationSecurityTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void apresentarSugestoes_comTamanhoExcedido_deveFalhar() {
        String sugestoesGigantes = "a".repeat(1001);
        var request = ApresentarSugestoesRequest.builder()
                .sugestoes(sugestoesGigantes)
                .build();

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("1000 caracteres"));
    }

    @Test
    void apresentarSugestoes_comTamanhoValido_devePassar() {
        String sugestoesValidas = "a".repeat(1000);
        var request = ApresentarSugestoesRequest.builder()
                .sugestoes(sugestoesValidas)
                .build();

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void devolverValidacao_comTamanhoExcedido_deveFalhar() {
        String justificativaGigante = "a".repeat(501);
        var request = DevolverValidacaoRequest.builder()
                .justificativa(justificativaGigante)
                .build();

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("500 caracteres"));
    }

    @Test
    void salvarMapa_comObservacoesExcedidas_deveFalhar() {
        String observacoesGigantes = "a".repeat(1001);
        var request = SalvarMapaRequest.builder()
                .observacoes(observacoesGigantes)
                .competencias(Collections.emptyList())
                .build();

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("1000 caracteres"));
    }

    @Test
    void competenciaMapa_comDescricaoExcedida_deveFalhar() {
        String descricaoGigante = "a".repeat(256);
        var dto = CompetenciaMapaDto.builder()
                .descricao(descricaoGigante)
                .atividadesCodigos(Collections.singletonList(1L))
                .build();

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("255 caracteres"));
    }

    @Test
    void submeterMapaAjustado_comJustificativaExcedida_deveFalhar() {
        String justificativaGigante = "a".repeat(501);
        var request = SubmeterMapaAjustadoRequest.builder()
                .justificativa(justificativaGigante)
                .build();

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("500 caracteres"));
    }
}
