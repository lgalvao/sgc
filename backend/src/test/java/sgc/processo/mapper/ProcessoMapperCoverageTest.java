package sgc.processo.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessoMapper Coverage Tests")
class ProcessoMapperCoverageTest {

    private final ProcessoMapper mapper = new ProcessoMapperImpl();

    @Test
    @DisplayName("Deve cobrir branches de tipo nulo no toEntity")
    void deveCobrirTipoNuloNoToEntity() {
        ProcessoDto dto = ProcessoDto.builder()
                .codigo(1L)
                .tipo(null)
                .build();

        Processo entity = mapper.toEntity(dto);
        assertThat(entity).isNotNull();
        assertThat(entity.getTipo()).isNull();
    }

    @Test
    @DisplayName("Deve cobrir branches de tipo nulo no toDto")
    void deveCobrirTipoNuloNoToDto() {
        Processo entity = new Processo();
        entity.setCodigo(1L);
        entity.setTipo(null);

        ProcessoDto dto = mapper.toDto(entity);
        assertThat(dto).isNotNull();
        assertThat(dto.getTipo()).isNull();
    }
}
