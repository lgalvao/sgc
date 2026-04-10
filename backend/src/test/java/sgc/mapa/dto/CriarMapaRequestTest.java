package sgc.mapa.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class CriarMapaRequestTest {

    @Test
    void deveConverterParaCommand() {
        var dataDisp = LocalDateTime.of(2023, 1, 1, 10, 0);
        var dataHomol = LocalDateTime.of(2023, 1, 2, 10, 0);

        var request = CriarMapaRequest.builder()
                .subprocessoCodigo(100L)
                .dataHoraDisponibilizado(dataDisp)
                .observacoesDisponibilizacao("obs")
                .sugestoes("sugestoes")
                .dataHoraHomologado(dataHomol)
                .build();

        var command = request.paraCommand();

        assertThat(command.subprocessoCodigo()).isEqualTo(100L);
        assertThat(command.estadoInicial()).isNotNull();
        assertThat(command.estadoInicial().dataHoraDisponibilizado()).isEqualTo(dataDisp);
        assertThat(command.estadoInicial().observacoesDisponibilizacao()).isEqualTo("obs");
        assertThat(command.estadoInicial().sugestoes()).isEqualTo("sugestoes");
        assertThat(command.estadoInicial().dataHoraHomologado()).isEqualTo(dataHomol);
    }
}
