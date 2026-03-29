package sgc.mapa.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ConhecimentoResumoDto")
class ConhecimentoResumoDtoTest {

    @Test
    @DisplayName("deve mapear conhecimento resumido")
    void deveMapearConhecimentoResumido() {
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(90L);
        conhecimento.setDescricao("Conhecimento resumido");

        ConhecimentoResumoDto dto = ConhecimentoResumoDto.fromEntity(conhecimento);

        assertThat(dto.codigo()).isEqualTo(90L);
        assertThat(dto.descricao()).isEqualTo("Conhecimento resumido");
    }
}
