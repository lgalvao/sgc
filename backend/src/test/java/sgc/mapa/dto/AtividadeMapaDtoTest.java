package sgc.mapa.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.mapa.MapaDtoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AtividadeMapaDto")
class AtividadeMapaDtoTest {

    private final MapaDtoMapper mapper = new MapaDtoMapper();

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

        AtividadeMapaDto dto = mapper.paraAtividadeMapaDto(atividade);

        assertThat(dto.codigo()).isEqualTo(20L);
        assertThat(dto.conhecimentos()).singleElement().satisfies(item -> {
            assertThat(item.codigo()).isEqualTo(30L);
            assertThat(item.descricao()).isEqualTo("Conhecimento");
        });
    }
}
