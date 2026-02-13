package sgc.organizacao.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Unidade;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("UsuarioMapper - Cobertura Adicional")
class UsuarioMapperCoverageTest {

    private final UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);

    @Test
    @DisplayName("mapUnidadeSiglaParaUsuario deve retornar null se unidade for nula")
    void deveRetornarNullSeUnidadeForNulaNoSigla() {
        assertThat(mapper.mapUnidadeSiglaParaUsuario(null)).isNull();
    }

    @Test
    @DisplayName("mapUnidadeNomeParaUsuario deve retornar null se unidade for nula")
    void deveRetornarNullSeUnidadeForNulaNoNome() {
        assertThat(mapper.mapUnidadeNomeParaUsuario(null)).isNull();
    }

    @Test
    @DisplayName("toUnidadeDto deve lidar com unidade nula")
    void deveLidarComUnidadeNulaNoToUnidadeDto() {
        // Quando toUnidadeDtoBase(null) retorna null, o branch if (dto != null) em toUnidadeDto(unidade, isElegivel) será false
        UnidadeDto dto = mapper.toUnidadeDto(null, true);
        assertThat(dto).isNull();
    }
}
