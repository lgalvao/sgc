package sgc.subprocesso.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de TipoTransicao")
class TipoTransicaoTest {

    @Test
    @DisplayName("Deve formatar alerta corretamente quando tem template")
    void deveFormatarAlertaComTemplate() {
        TipoTransicao tipo = TipoTransicao.CADASTRO_DISPONIBILIZADO;
        assertThat(tipo.geraAlerta()).isTrue();

        String alerta = tipo.formatarAlerta("SIGLA");
        assertThat(alerta).contains("SIGLA").contains("disponibilizado");
    }

    @Test
    @DisplayName("Deve retornar null ao formatar alerta quando não tem template")
    void deveRetornarNullAlertaSemTemplate() {
        TipoTransicao tipo = TipoTransicao.CADASTRO_HOMOLOGADO;
        assertThat(tipo.geraAlerta()).isFalse();

        String alerta = tipo.formatarAlerta("SIGLA");
        assertThat(alerta).isEqualTo("");
    }
}
