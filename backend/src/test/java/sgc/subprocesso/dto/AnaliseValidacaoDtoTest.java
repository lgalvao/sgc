package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AnaliseValidacaoDto Tests")
class AnaliseValidacaoDtoTest {

    @Test
    @DisplayName("Deve cobrir o construtor e getters do record")
    void deveCobrirConstrutorEGetters() {
        LocalDateTime agora = LocalDateTime.now();
        AnaliseValidacaoDto dto = new AnaliseValidacaoDto(1L, agora, "Obs", "Acao", "SIGLA");

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.dataHora()).isEqualTo(agora);
        assertThat(dto.observacoes()).isEqualTo("Obs");
        assertThat(dto.acao()).isEqualTo("Acao");
        assertThat(dto.unidadeSigla()).isEqualTo("SIGLA");
    }

    @Test
    @DisplayName("Deve cobrir equals, hashCode e toString")
    void deveCobrirMetodosPadrao() {
        AnaliseValidacaoDto dto1 = new AnaliseValidacaoDto(1L, null, null, null, null);
        AnaliseValidacaoDto dto2 = new AnaliseValidacaoDto(1L, null, null, null, null);

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2)
                .hasToString(dto2.toString());
    }
}
