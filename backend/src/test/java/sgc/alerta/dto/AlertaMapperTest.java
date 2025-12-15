package sgc.alerta.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.model.Alerta;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AlertaMapperTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private AlertaMapperImpl mapper;

    @Test
    @DisplayName("formatDataHora returns formatted string")
    void formatDataHora() {
        LocalDateTime dt = LocalDateTime.of(2023, 10, 25, 14, 30);
        assertThat(mapper.formatDataHora(dt)).isEqualTo("25/10/2023 14:30");
    }

    @Test
    @DisplayName("formatDataHora returns empty string if null")
    void formatDataHoraNull() {
        assertThat(mapper.formatDataHora(null)).isEqualTo("");
    }

    @Test
    @DisplayName("extractProcessoName returns name")
    void extractProcessoName() {
        String desc = "Início do processo 'Mapeamento 2023'. Preencha...";
        assertThat(mapper.extractProcessoName(desc)).isEqualTo("Mapeamento 2023");
    }

    @Test
    @DisplayName("extractProcessoName returns empty if no match")
    void extractProcessoNameNoMatch() {
        String desc = "Outra descrição";
        assertThat(mapper.extractProcessoName(desc)).isEqualTo("");
    }

    static class AlertaMapperImpl extends AlertaMapper {
        @Override
        public AlertaDto toDto(Alerta alerta) {
            return null;
        }
    }
}
