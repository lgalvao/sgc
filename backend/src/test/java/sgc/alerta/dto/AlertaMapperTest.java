package sgc.alerta.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários: AlertaMapper")
class AlertaMapperTest {

    private final AlertaMapper mapper = new AlertaMapperImpl();

    @Test
    @DisplayName("formatDataHora deve retornar string formatada")
    void formatDataHora() {
        LocalDateTime dt = LocalDateTime.of(2023, 10, 25, 14, 30, 15);
        assertThat(mapper.formatDataHora(dt)).isEqualTo("25/10/2023 14:30:15");
    }

    @Test
    @DisplayName("formatDataHora deve retornar string vazia se nulo")
    void formatDataHoraNull() {
        assertThat(mapper.formatDataHora(null)).isEmpty();
    }

    @Test
    @DisplayName("extractProcessoName deve retornar nome do processo quando padrão casar")
    void extractProcessoName() {
        String descricao = "Alteração no processo 'Processo Teste' realizada com sucesso.";
        assertThat(mapper.extractProcessoName(descricao)).isEqualTo("Processo Teste");
    }

    @Test
    @DisplayName("extractProcessoName deve retornar string vazia se descrição for nula")
    void extractProcessoNameNull() {
        assertThat(mapper.extractProcessoName(null)).isEmpty();
    }

    @Test
    @DisplayName("extractProcessoName deve retornar string vazia se padrão não casar")
    void extractProcessoNameSemMatch() {
        String descricao = "Descrição sem nome de processo entre aspas simples.";
        assertThat(mapper.extractProcessoName(descricao)).isEmpty();
    }

    static class AlertaMapperImpl extends AlertaMapper {
        @Override
        public AlertaDto toDto(sgc.alerta.model.Alerta alerta) {
            return null; // Note: MapStruct generates the real implementation
        }
    }
}
