package sgc.organizacao.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UnidadeDto")
class UnidadeDtoTest {
    private final OrganizacaoDtoMapper mapper = new OrganizacaoDtoMapper();


    @Test
    @DisplayName("deve retornar nulo quando entidade for nula")
    void deveRetornarNuloQuandoEntidadeForNula() {
        assertThat(mapper.paraUnidadeDto(null)).isNull();
    }

    @Test
    @DisplayName("deve mapear unidade com responsavel")
    void deveMapearUnidadeComResponsavel() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setMatricula("0001");
        usuario.setNome("Maria");
        usuario.setEmail("maria@tre.jus.br");
        usuario.setRamal("1234");

        Responsabilidade responsabilidade = new Responsabilidade();
        responsabilidade.setUsuario(usuario);

        Unidade pai = new Unidade();
        pai.setCodigo(1L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setUnidadeSuperior(pai);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("999");
        unidade.setResponsabilidade(responsabilidade);

        UnidadeDto dto = mapper.paraUnidadeDto(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(2L);
        assertThat(dto.getCodigoPai()).isEqualTo(1L);
        assertThat(dto.getTipo()).isEqualTo("OPERACIONAL");
        assertThat(dto.getResponsavel()).isNotNull();
        assertThat(dto.getResponsavel().nome()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("deve mapear unidade obrigatoria")
    void deveMapearUnidadeObrigatoria() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        UnidadeDto dto = mapper.paraUnidadeDtoObrigatoria(unidade);

        assertThat(dto.getCodigo()).isEqualTo(2L);
        assertThat(dto.getSigla()).isEqualTo("UND");
        assertThat(dto.getTipo()).isEqualTo("OPERACIONAL");
    }

    @Test
    @DisplayName("deve mapear unidade resumida obrigatoria")
    void deveMapearUnidadeResumidaObrigatoria() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("Titular");

        UnidadeDto dto = mapper.paraUnidadeDtoResumoObrigatoria(unidade);

        assertThat(dto.getCodigo()).isEqualTo(2L);
        assertThat(dto.getNome()).isEqualTo("Unidade");
        assertThat(dto.getSigla()).isEqualTo("UND");
        assertThat(dto.getTipo()).isEqualTo("OPERACIONAL");
        assertThat(dto.getTituloTitular()).isEqualTo("Titular");
        assertThat(dto.getCodigoPai()).isNull();
        assertThat(dto.getResponsavel()).isNull();
    }

    @Test
    @DisplayName("deve falhar ao mapear unidade resumida nula")
    void deveFalharAoMapearUnidadeResumidaNula() {
        assertThatThrownBy(() -> mapper.paraUnidadeDtoResumoObrigatoria(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Unidade obrigatoria para resumo");
    }
}
