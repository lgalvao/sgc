package sgc.configuracoes;

import org.junit.jupiter.api.*;
import sgc.configuracoes.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ConfiguracaoDto")
class ConfiguracaoDtoTest {

    @Test
    @DisplayName("deve mapear parametro para dto")
    void deveMapearParametroParaDto() {
        Configuracao configuracao = new Configuracao();
        configuracao.setCodigo(1L);
        configuracao.setChave("tema");
        configuracao.setDescricao("Tema atual");
        configuracao.setValor("claro");

        ConfiguracaoDto dto = ConfiguracaoDto.fromEntity(configuracao);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.chave()).isEqualTo("tema");
        assertThat(dto.valor()).isEqualTo("claro");
    }
}
