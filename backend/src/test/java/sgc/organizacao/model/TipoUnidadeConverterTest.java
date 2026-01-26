package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
@DisplayName("Testes do Conversor de Tipo de Unidade")
class TipoUnidadeConverterTest {

    private final TipoUnidadeConverter converter = new TipoUnidadeConverter();

    @Test
    @DisplayName("Deve converter para coluna do banco")
    void deveConverterParaColunaBanco() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(TipoUnidade.OPERACIONAL)).isEqualTo("OPERACIONAL");
        assertThat(converter.convertToDatabaseColumn(TipoUnidade.SEM_EQUIPE)).isEqualTo("SEM EQUIPE");
    }

    @Test
    @DisplayName("Deve converter para atributo da entidade")
    void deveConverterParaAtributoEntidade() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("OPERACIONAL")).isEqualTo(TipoUnidade.OPERACIONAL);
        assertThat(converter.convertToEntityAttribute("SEM EQUIPE")).isEqualTo(TipoUnidade.SEM_EQUIPE);
    }

    @Test
    @DisplayName("Deve lançar exceção para valor desconhecido")
    void deveLancarExcecaoParaValorDesconhecido() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown TipoUnidade: INVALIDO");
    }
}
