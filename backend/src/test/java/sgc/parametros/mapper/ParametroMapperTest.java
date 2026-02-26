package sgc.parametros.mapper;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.*;
import sgc.parametros.*;
import sgc.parametros.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ParametroMapper Tests")
class ParametroMapperTest {
    private final ParametroMapper mapper = Mappers.getMapper(ParametroMapper.class);

    @Test
    @DisplayName("Deve lidar com null no atualizarEntidade")
    void deveLidarComNullNoAtualizar() {
        Parametro p = new Parametro();
        p.setValor("valor");
        mapper.atualizarEntidade(null, p);
        assertThat(p.getValor()).isEqualTo("valor");
    }
}
