package sgc.mapa.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AtividadeMapaDto")
class AtividadeMapaDtoTest {

    @Test
    @DisplayName("deve mapear atividade com conhecimentos resumidos")
    void deveMapearAtividadeComConhecimentosResumidos() {
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(30L);
        conhecimento.setDescricao("Conhecimento");

        Atividade atividade = new Atividade();
        atividade.setCodigo(20L);
        atividade.setDescricao("Atividade");
        atividade.setConhecimentos(new LinkedHashSet<>(List.of(conhecimento)));

        AtividadeMapaDto dto = AtividadeMapaDto.fromEntity(atividade);

        assertThat(dto.codigo()).isEqualTo(20L);
        assertThat(dto.conhecimentos()).singleElement().satisfies(item -> {
            assertThat(item.codigo()).isEqualTo(30L);
            assertThat(item.descricao()).isEqualTo("Conhecimento");
        });
    }
}
