package sgc.subprocesso.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnaliseValidacaoDto Coverage Tests")
class AnaliseValidacaoDtoCoverageTest {

    @Test
    @DisplayName("Deve cobrir o construtor e getters do record")
    void deveCobrirConstrutorEGetters() {
        LocalDateTime agora = LocalDateTime.now();
        AnaliseValidacaoDto dto = new AnaliseValidacaoDto(1L, agora, "01/01/2026", "Obs", "Acao", "SIGLA");

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.dataHora()).isEqualTo(agora);
        assertThat(dto.dataHoraFormatada()).isEqualTo("01/01/2026");
        assertThat(dto.observacoes()).isEqualTo("Obs");
        assertThat(dto.acao()).isEqualTo("Acao");
        assertThat(dto.unidadeSigla()).isEqualTo("SIGLA");
    }

    @Test
    @DisplayName("Deve cobrir equals, hashCode e toString")
    void deveCobrirMetodosPadrao() {
        AnaliseValidacaoDto dto1 = new AnaliseValidacaoDto(1L, null, null, null, null, null);
        AnaliseValidacaoDto dto2 = new AnaliseValidacaoDto(1L, null, null, null, null, null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).isNotNull();
    }
}
