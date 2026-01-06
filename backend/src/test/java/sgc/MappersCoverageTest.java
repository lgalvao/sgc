package sgc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.mapper.UsuarioMapperImpl;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.mapper.ConhecimentoMapperImpl;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cobertura de Mappers")
class MappersCoverageTest {

    private final UsuarioMapper usuarioMapper = new UsuarioMapperImpl();
    private final ConhecimentoMapper conhecimentoMapper = new ConhecimentoMapperImpl();

    @Test
    @DisplayName("Deve cobrir nulos no UsuarioMapper")
    void deveCobrirNulosUsuarioMapper() {
        assertThat(usuarioMapper.toUnidadeDto(null, true)).isNotNull();
        assertThat(usuarioMapper.toUsuarioDto(null)).isNotNull();
        assertThat(usuarioMapper.toAtribuicaoTemporariaDto(null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir nulos no ConhecimentoMapper")
    void deveCobrirNulosConhecimentoMapper() {
        assertThat(conhecimentoMapper.toDto(null)).isNull();
        assertThat(conhecimentoMapper.toDtoList(null)).isNull();
    }
}
