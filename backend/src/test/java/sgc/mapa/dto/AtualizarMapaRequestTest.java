package sgc.mapa.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class AtualizarMapaRequestTest {

    @Test
    void deveConverterParaCommand() {
        var dataDisp = LocalDateTime.of(2023, 1, 1, 10, 0);
        var dataHomol = LocalDateTime.of(2023, 1, 2, 10, 0);

        var request = AtualizarMapaRequest.builder()
                .dataHoraDisponibilizado(dataDisp)
                .observacoesDisponibilizacao("obs")
                .sugestoes("sugestoes")
                .dataHoraHomologado(dataHomol)
                .build();

        var command = request.paraCommand();

        assertThat(command.dataHoraDisponibilizado()).isEqualTo(dataDisp);
        assertThat(command.observacoesDisponibilizacao()).isEqualTo("obs");
        assertThat(command.sugestoes()).isEqualTo("sugestoes");
        assertThat(command.dataHoraHomologado()).isEqualTo(dataHomol);
    }
}
