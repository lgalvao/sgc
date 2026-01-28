package sgc.processo.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ProcessoMapperCoverageTest {

    private final ProcessoMapper mapper = Mappers.getMapper(ProcessoMapper.class);

    @Test
    @DisplayName("mapUnidadesParticipantes deve lidar com retorno null de getParticipantes")
    void mapUnidadesParticipantes_NullParticipantes() {
        // Arrange
        Processo processo = mock(Processo.class);
        when(processo.getParticipantes()).thenReturn(null);
        ProcessoDto dto = new ProcessoDto();

        // Act
        mapper.mapUnidadesParticipantes(processo, dto);

        // Assert
        assertThat(dto.getUnidadesParticipantes()).isNull();
    }
}
