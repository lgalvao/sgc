package sgc.subprocesso.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SugestoesDtoTest {

    @Test
    void vazia_deveRetornarSugestoesVazia() {
        SugestoesDto dto = SugestoesDto.vazia();

        assertThat(dto.sugestoes()).isEmpty();
    }

    @Test
    void de_comStringNula_deveRetornarSugestoesVazia() {
        SugestoesDto dto = SugestoesDto.de(null);

        assertThat(dto.sugestoes()).isEmpty();
    }

    @Test
    void de_comStringValida_deveRetornarStringCorreta() {
        SugestoesDto dto = SugestoesDto.de("Minha sugestão");

        assertThat(dto.sugestoes()).isEqualTo("Minha sugestão");
    }
}
