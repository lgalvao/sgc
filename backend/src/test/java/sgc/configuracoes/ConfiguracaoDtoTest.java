package sgc.configuracoes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.configuracoes.model.Configuracao;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfiguracaoDto")
class ConfiguracaoDtoTest {

    private final ConfiguracaoMapper mapper = new ConfiguracaoMapper();

    @Test
    @DisplayName("deve mapear parametro para dto")
    void deveMapearParametroParaDto() {
        Configuracao configuracao = new Configuracao();
        configuracao.setCodigo(1L);
        configuracao.setChave("tema");
        configuracao.setDescricao("Tema atual");
        configuracao.setValor("claro");

        ConfiguracaoDto dto = mapper.paraDto(configuracao);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.chave()).isEqualTo("tema");
        assertThat(dto.valor()).isEqualTo("claro");
    }
}
