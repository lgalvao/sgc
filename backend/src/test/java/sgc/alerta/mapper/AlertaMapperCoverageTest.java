package sgc.alerta.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.alerta.dto.AlertaDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("AlertaMapper - Cobertura Adicional")
class AlertaMapperCoverageTest {

    private final AlertaMapper mapper = Mappers.getMapper(AlertaMapper.class);

    @Test
    @DisplayName("toDto com dataHoraLeitura deve retornar null se alerta for nulo")
    void deveRetornarNullSeAlertaForNulo() {
        // Quando toDto(null) retorna null, o branch if (dto == null) será true
        AlertaDto dto = mapper.toDto(null, LocalDateTime.now());
        assertThat(dto).isNull();
    }
}
