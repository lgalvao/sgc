package sgc.subprocesso.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.subprocesso.model.Subprocesso;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("SubprocessoMapper Tests")
class SubprocessoMapperTest {

    private final SubprocessoMapper mapper = Mappers.getMapper(SubprocessoMapper.class);

    @Test
    @DisplayName("Deve retornar null quando subprocesso é nulo")
    void deveRetornarNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("Deve mapear campos nulos corretamente")
    void deveMapearCamposNulos() {
        Subprocesso s = new Subprocesso();
        s.setProcesso(null);
        s.setUnidade(null);
        s.setMapa(null);

        var dto = mapper.toDto(s);
        assertThat(dto.getCodProcesso()).isNull();
        assertThat(dto.getCodUnidade()).isNull();
        assertThat(dto.getCodMapa()).isNull();
    }
}
