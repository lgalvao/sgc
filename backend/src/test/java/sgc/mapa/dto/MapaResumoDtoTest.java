package sgc.mapa.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MapaResumoDto")
class MapaResumoDtoTest {

    @Test
    @DisplayName("deve mapear resumo do mapa")
    void deveMapearResumoDoMapa() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(77L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(88L);
        mapa.setSubprocesso(subprocesso);
        mapa.setDataHoraDisponibilizado(LocalDateTime.of(2025, 1, 2, 10, 0));
        mapa.setObservacoesDisponibilizacao("Obs");
        mapa.setSugestoes("Sugestões");
        mapa.setDataHoraHomologado(LocalDateTime.of(2025, 1, 3, 10, 0));

        MapaResumoDto dto = MapaResumoDto.fromEntity(mapa);

        assertThat(dto.codigo()).isEqualTo(88L);
        assertThat(dto.subprocessoCodigo()).isEqualTo(77L);
        assertThat(dto.observacoesDisponibilizacao()).isEqualTo("Obs");
        assertThat(dto.sugestoes()).isEqualTo("Sugestões");
        assertThat(dto.dataHoraDisponibilizado()).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 0));
        assertThat(dto.dataHoraHomologado()).isEqualTo(LocalDateTime.of(2025, 1, 3, 10, 0));
    }
}
