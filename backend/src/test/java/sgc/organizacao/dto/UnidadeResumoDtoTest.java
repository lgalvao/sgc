package sgc.organizacao.dto;

import org.junit.jupiter.api.Test;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnidadeResumoDtoTest {

    @Test
    void deveCriarAPartirDeEntidade() {
        var unidade = Unidade.builder()
                .codigo(1L)
                .nome("Nome da Unidade")
                .sigla("SIG")
                .tipo(TipoUnidade.OPERACIONAL)
                .tituloTitular("Chefe")
                .build();

        var dto = UnidadeResumoDto.fromEntityObrigatoria(unidade);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.nome()).isEqualTo("Nome da Unidade");
        assertThat(dto.sigla()).isEqualTo("SIG");
        assertThat(dto.tipo()).isEqualTo("OPERACIONAL");
        assertThat(dto.tituloTitular()).isEqualTo("Chefe");
    }

    @Test
    void deveCriarAPartirDeEntidadeComCamposNulos() {
        var unidade = Unidade.builder()
                .codigo(1L)
                .nome("Nome da Unidade")
                .sigla("SIG")
                .build();

        var dto = UnidadeResumoDto.fromEntityObrigatoria(unidade);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.nome()).isEqualTo("Nome da Unidade");
        assertThat(dto.sigla()).isEqualTo("SIG");
        assertThat(dto.tipo()).isNull();
        assertThat(dto.tituloTitular()).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoEntidadeForNula() {
        assertThatThrownBy(() -> UnidadeResumoDto.fromEntityObrigatoria(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Unidade obrigatoria para resumo");
    }

    @Test
    void deveLancarExcecaoQuandoCamposObrigatoriosFaltarem() {
        assertThatThrownBy(() -> UnidadeResumoDto.fromResumoObrigatorio(null, "Nome", "SIG", TipoUnidade.OPERACIONAL, "Chefe"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Codigo da unidade obrigatorio");

        assertThatThrownBy(() -> UnidadeResumoDto.fromResumoObrigatorio(1L, null, "SIG", TipoUnidade.OPERACIONAL, "Chefe"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Nome da unidade obrigatorio");

        assertThatThrownBy(() -> UnidadeResumoDto.fromResumoObrigatorio(1L, "Nome", null, TipoUnidade.OPERACIONAL, "Chefe"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Sigla da unidade obrigatoria");
    }
}
