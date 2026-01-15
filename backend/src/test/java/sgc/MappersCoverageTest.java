package sgc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.organizacao.mapper.UsuarioMapper;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cobertura de Mappers")
@Tag("unit")
class MappersCoverageTest {
    private final ConhecimentoMapper conhecimentoMapper = Mappers.getMapper(ConhecimentoMapper.class);
    private final UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);

    @Test
    @DisplayName("Deve cobrir nulos no UsuarioMapper")
    void deveCobrirNulosUsuarioMapper() {
        assertThat(usuarioMapper.toUnidadeDto(null, true)).isNull();
        assertThat(usuarioMapper.toUsuarioDto(null)).isNull();
        assertThat(usuarioMapper.toAtribuicaoTemporariaDto(null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir nulos no ConhecimentoMapper")
    void deveCobrirNulosConhecimentoMapper() {
        assertThat(conhecimentoMapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir método map do ConhecimentoMapper")
    void deveCobrirMetodoMap() {
        // Inject mock repo
        sgc.mapa.model.AtividadeRepo repo = org.mockito.Mockito.mock(sgc.mapa.model.AtividadeRepo.class);
        try {
            java.lang.reflect.Field field = sgc.mapa.mapper.ConhecimentoMapper.class.getDeclaredField("atividadeRepo");
            field.setAccessible(true);
            field.set(conhecimentoMapper, repo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Test null
        assertThat(conhecimentoMapper.map(null)).isNull();

        // Test exception
        org.mockito.Mockito.when(repo.findById(1L)).thenReturn(java.util.Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> conhecimentoMapper.map(1L))
                .isInstanceOf(sgc.comum.erros.ErroEntidadeDeveriaExistir.class);

        // Test success
        org.mockito.Mockito.when(repo.findById(2L)).thenReturn(java.util.Optional.of(new sgc.mapa.model.Atividade()));
        assertThat(conhecimentoMapper.map(2L)).isNotNull();
    }
}
