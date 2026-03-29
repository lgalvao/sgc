package sgc.mapa.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AtividadeDto")
class AtividadeDtoTest {

    @Test
    @DisplayName("deve mapear atividade com conhecimentos")
    void deveMapearAtividadeComConhecimentos() {
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(20L);
        conhecimento.setDescricao("Conhecimento");

        Atividade atividade = new Atividade();
        atividade.setCodigo(10L);
        atividade.setDescricao("Atividade");
        atividade.setConhecimentos(new LinkedHashSet<>(List.of(conhecimento)));

        AtividadeDto dto = AtividadeDto.fromEntity(atividade);

        assertThat(dto.codigo()).isEqualTo(10L);
        assertThat(dto.descricao()).isEqualTo("Atividade");
        assertThat(dto.conhecimentos()).singleElement().satisfies(item -> {
            assertThat(item.codigo()).isEqualTo(20L);
            assertThat(item.descricao()).isEqualTo("Conhecimento");
        });
    }
}
