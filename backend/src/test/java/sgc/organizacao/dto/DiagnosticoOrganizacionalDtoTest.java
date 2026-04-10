package sgc.organizacao.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DiagnosticoOrganizacionalDtoTest {

    @Test
    void deveCriarDiagnosticoSemViolacoes() {
        var diagnostico = DiagnosticoOrganizacionalDto.semViolacoes();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
        assertThat(diagnostico.resumo()).isEmpty();
        assertThat(diagnostico.quantidadeTiposViolacao()).isZero();
        assertThat(diagnostico.quantidadeOcorrencias()).isZero();
        assertThat(diagnostico.grupos()).isEmpty();
    }
}
