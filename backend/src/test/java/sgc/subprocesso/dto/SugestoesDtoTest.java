package sgc.subprocesso.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SugestoesDto - Cobertura de Testes")
class SugestoesDtoTest {

    @Test
    @DisplayName("vazia deve retornar DTO com string vazia")
    void vazia() {
        SugestoesDto dto = SugestoesDto.vazia();
        assertThat(dto.sugestoes()).isEmpty();
    }

    @Test
    @DisplayName("de deve retornar string recebida quando nao nula")
    void de_NaoNula() {
        SugestoesDto dto = SugestoesDto.de("Sugestao Teste");
        assertThat(dto.sugestoes()).isEqualTo("Sugestao Teste");
    }

    @Test
    @DisplayName("de deve retornar string vazia quando nula")
    void de_Nula() {
        SugestoesDto dto = SugestoesDto.de(null);
        assertThat(dto.sugestoes()).isEmpty();
    }
}
