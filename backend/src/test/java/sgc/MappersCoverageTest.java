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
        sgc.comum.repo.RepositorioComum repo = org.mockito.Mockito.mock(sgc.comum.repo.RepositorioComum.class);
        try {
            java.lang.reflect.Field field = sgc.mapa.mapper.ConhecimentoMapper.class.getDeclaredField("repo");
            field.setAccessible(true);
            field.set(conhecimentoMapper, repo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Test null
        assertThat(conhecimentoMapper.map(null)).isNull();

        // Test exception
        org.mockito.Mockito.when(repo.buscar(sgc.mapa.model.Atividade.class, 1L))
                .thenThrow(new sgc.comum.erros.ErroEntidadeNaoEncontrada("Atividade", 1L));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> conhecimentoMapper.map(1L))
                .isInstanceOf(sgc.comum.erros.ErroEntidadeNaoEncontrada.class);

        // Test success
        org.mockito.Mockito.when(repo.buscar(sgc.mapa.model.Atividade.class, 2L))
                .thenReturn(new sgc.mapa.model.Atividade());
        assertThat(conhecimentoMapper.map(2L)).isNotNull();
    }
}
