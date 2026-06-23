package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class SugestoesDtoTest {

    @Test
    void shouldCreateVazia() {
        SugestoesDto dto = SugestoesDto.vazia();
        assertThat(dto.sugestoes()).isEmpty();
    }

    @Test
    void shouldCreateDeWithText() {
        String text = "Algumas sugestões";
        SugestoesDto dto = SugestoesDto.de(text);
        assertThat(dto.sugestoes()).isEqualTo(text);
    }

    @Test
    void shouldCreateDeWithNull() {
        SugestoesDto dto = SugestoesDto.de(null);
        assertThat(dto.sugestoes()).isEmpty();
    }
}
