package sgc.mapa.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.mapa.MapaDtoMapper;
import sgc.mapa.model.Conhecimento;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConhecimentoResumoDto")
class ConhecimentoResumoDtoTest {

    private final MapaDtoMapper mapper = new MapaDtoMapper();

    @Test
    @DisplayName("deve mapear conhecimento resumido")
    void deveMapearConhecimentoResumido() {
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(90L);
        conhecimento.setDescricao("Conhecimento resumido");

        ConhecimentoResumoDto dto = mapper.paraConhecimentoResumoDto(conhecimento);

        assertThat(dto.codigo()).isEqualTo(90L);
        assertThat(dto.descricao()).isEqualTo("Conhecimento resumido");
    }
}
