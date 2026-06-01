package sgc.organizacao.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;

import static org.assertj.core.api.Assertions.*;

class UnidadeResumoDtoTest {
    private final OrganizacaoDtoMapper mapper = new OrganizacaoDtoMapper();


    @Test
    void deveCriarAPartirDeEntidade() {
        var unidade = Unidade.builder()
                .codigo(1L)
                .nome("Nome da Unidade")
                .sigla("SIG")
                .tipo(TipoUnidade.OPERACIONAL)
                .tituloTitular("Chefe")
                .build();

        var dto = mapper.paraUnidadeResumoObrigatoria(unidade);

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

        var dto = mapper.paraUnidadeResumoObrigatoria(unidade);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.nome()).isEqualTo("Nome da Unidade");
        assertThat(dto.sigla()).isEqualTo("SIG");
        assertThat(dto.tipo()).isNull();
        assertThat(dto.tituloTitular()).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoEntidadeForNula() {
        assertThatThrownBy(() -> mapper.paraUnidadeResumoObrigatoria(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Unidade obrigatoria para resumo");
    }

    @Test
    void deveLancarExcecaoQuandoCamposObrigatoriosFaltarem() {
        assertThatThrownBy(() -> UnidadeResumoDto.fromResumoObrigatorio(null, "Nome", "SIG", TipoUnidade.OPERACIONAL.name(), "Chefe"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Codigo da unidade obrigatorio");

        assertThatThrownBy(() -> UnidadeResumoDto.fromResumoObrigatorio(1L, null, "SIG", TipoUnidade.OPERACIONAL.name(), "Chefe"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Nome da unidade obrigatorio");

        assertThatThrownBy(() -> UnidadeResumoDto.fromResumoObrigatorio(1L, "Nome", null, TipoUnidade.OPERACIONAL.name(), "Chefe"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Sigla da unidade obrigatoria");
    }
}
