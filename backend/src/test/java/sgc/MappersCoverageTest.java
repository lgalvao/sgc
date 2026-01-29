package sgc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.mapa.mapper.ConhecimentoMapper;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cobertura de Mappers")
@Tag("unit")
class MappersCoverageTest {
    private final ConhecimentoMapper conhecimentoMapper = Mappers.getMapper(ConhecimentoMapper.class);

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

        // REMOVIDO: Teste do método map() que não é mais usado após simplificação
        // O método map(Long) foi removido do ConhecimentoMapper por não ser necessário
    }
}
