package sgc.subprocesso.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class AtualizarSubprocessoRequestTest {

    @Test
    void deveConverterParaCommandComTodosCampos() {
        var dataLim1 = LocalDateTime.of(2023, 1, 1, 10, 0);
        var dataFim1 = LocalDateTime.of(2023, 1, 2, 10, 0);
        var dataLim2 = LocalDateTime.of(2023, 1, 3, 10, 0);
        var dataFim2 = LocalDateTime.of(2023, 1, 4, 10, 0);

        var request = AtualizarSubprocessoRequest.builder()
                .codUnidade(1L)
                .codMapa(2L)
                .dataLimiteEtapa1(dataLim1)
                .dataFimEtapa1(dataFim1)
                .dataLimiteEtapa2(dataLim2)
                .dataFimEtapa2(dataFim2)
                .build();

        var command = request.paraCommand();

        assertThat(command.vinculos()).isNotNull();
        assertThat(command.vinculos().codUnidade()).isEqualTo(1L);
        assertThat(command.vinculos().codMapa()).isEqualTo(2L);

        assertThat(command.prazos()).isNotNull();
        assertThat(command.prazos().dataLimiteEtapa1()).isPresent().contains(dataLim1);
        assertThat(command.prazos().dataFimEtapa1()).isPresent().contains(dataFim1);
        assertThat(command.prazos().dataLimiteEtapa2()).isPresent().contains(dataLim2);
        assertThat(command.prazos().dataFimEtapa2()).isPresent().contains(dataFim2);
    }

    @Test
    void deveConverterParaCommandComCamposNulos() {
        var request = AtualizarSubprocessoRequest.builder()
                .codUnidade(null)
                .codMapa(null)
                .dataLimiteEtapa1(null)
                .dataFimEtapa1(null)
                .dataLimiteEtapa2(null)
                .dataFimEtapa2(null)
                .build();

        var command = request.paraCommand();

        assertThat(command.vinculos()).isNotNull();
        assertThat(command.vinculos().codUnidade()).isNull();
        assertThat(command.vinculos().codMapa()).isNull();

        assertThat(command.prazos()).isNotNull();
        assertThat(command.prazos().dataLimiteEtapa1()).isEmpty();
        assertThat(command.prazos().dataFimEtapa1()).isEmpty();
        assertThat(command.prazos().dataLimiteEtapa2()).isEmpty();
        assertThat(command.prazos().dataFimEtapa2()).isEmpty();
    }
}
