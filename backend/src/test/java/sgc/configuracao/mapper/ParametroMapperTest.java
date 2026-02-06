package sgc.configuracao.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.configuracao.model.Parametro;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("ParametroMapper Tests")
class ParametroMapperTest {

    private final ParametroMapper mapper = Mappers.getMapper(ParametroMapper.class);

    @Test
    @DisplayName("Deve retornar null no toResponse")
    void deveRetornarNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("Deve lidar com null no atualizarEntidade")
    void deveLidarComNullNoAtualizar() {
        Parametro p = new Parametro();
        p.setValor("valor");
        mapper.atualizarEntidade(null, p);
        assertThat(p.getValor()).isEqualTo("valor");
    }
}
