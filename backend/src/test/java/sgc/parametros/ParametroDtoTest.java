package sgc.parametros;

import org.junit.jupiter.api.*;
import sgc.parametros.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ParametroDto")
class ParametroDtoTest {

    @Test
    @DisplayName("deve mapear parametro para dto")
    void deveMapearParametroParaDto() {
        Parametro parametro = new Parametro();
        parametro.setCodigo(1L);
        parametro.setChave("tema");
        parametro.setDescricao("Tema atual");
        parametro.setValor("claro");

        ParametroDto dto = ParametroDto.fromEntity(parametro);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.chave()).isEqualTo("tema");
        assertThat(dto.valor()).isEqualTo("claro");
    }
}
