package sgc.mapa.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.mapa.MapaDtoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AtividadeDto")
class AtividadeDtoTest {

    private final MapaDtoMapper mapper = new MapaDtoMapper();

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

        AtividadeDto dto = mapper.paraAtividadeDto(atividade);

        assertThat(dto.codigo()).isEqualTo(10L);
        assertThat(dto.descricao()).isEqualTo("Atividade");
        assertThat(dto.conhecimentos()).singleElement().satisfies(item -> {
            assertThat(item.codigo()).isEqualTo(20L);
            assertThat(item.descricao()).isEqualTo("Conhecimento");
        });
    }
}
