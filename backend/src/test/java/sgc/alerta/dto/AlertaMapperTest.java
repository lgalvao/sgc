package sgc.alerta.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.alerta.mapper.AlertaMapper;
import sgc.alerta.model.Alerta;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AlertaMapper")
class AlertaMapperTest {

    private final AlertaMapper mapper = new AlertaMapperStub();

    @Test
    @DisplayName("formatDataHora deve retornar string formatada")
    void formatDataHora() {
        LocalDateTime dt = LocalDateTime.of(2023, 10, 25, 14, 30, 15);
        AlertaMapperStub stub = (AlertaMapperStub) mapper;
        assertThat(stub.publicFormatDataHora(dt)).isEqualTo("25/10/2023 14:30:15");
    }

    @Test
    @DisplayName("formatDataHora deve retornar string vazia se nulo")
    void formatDataHoraNull() {
        AlertaMapperStub stub = (AlertaMapperStub) mapper;
        assertThat(stub.publicFormatDataHora(null)).isEmpty();
    }

    @Test
    @DisplayName("toDto com dataHoraLeitura deve incluir o campo")
    void toDtoComDataHoraLeitura() {
        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);
        LocalDateTime dataLeitura = LocalDateTime.of(2023, 10, 25, 15, 0, 0);
        
        AlertaDto dto = mapper.toDto(alerta, dataLeitura);
        
        assertThat(dto.getDataHoraLeitura()).isEqualTo(dataLeitura);
    }

    static class AlertaMapperStub extends AlertaMapper {
        @Override
        public AlertaDto toDto(Alerta alerta) {
            if (alerta == null) return null;
            return AlertaDto.builder()
                    .codigo(alerta.getCodigo())
                    .build();
        }
        
        // Expõe o método protegido para testes
        public String publicFormatDataHora(LocalDateTime dataHora) {
            return formatDataHora(dataHora);
        }
    }
}
