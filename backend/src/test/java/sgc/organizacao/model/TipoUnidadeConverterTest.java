package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do TipoUnidadeConverter")
class TipoUnidadeConverterTest {

    private final TipoUnidadeConverter converter = new TipoUnidadeConverter();

    @Test
    @DisplayName("Deve converter TipoUnidade para coluna do banco")
    void deveConverterParaColunaBanco() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals("SEM EQUIPE", converter.convertToDatabaseColumn(TipoUnidade.SEM_EQUIPE));
        assertEquals("OPERACIONAL", converter.convertToDatabaseColumn(TipoUnidade.OPERACIONAL));
        assertEquals("INTERMEDIARIA", converter.convertToDatabaseColumn(TipoUnidade.INTERMEDIARIA));
    }

    @Test
    @DisplayName("Deve converter coluna do banco para TipoUnidade")
    void deveConverterParaAtributoEntidade() {
        assertNull(converter.convertToEntityAttribute(null));
        assertEquals(TipoUnidade.SEM_EQUIPE, converter.convertToEntityAttribute("SEM EQUIPE"));
        assertEquals(TipoUnidade.OPERACIONAL, converter.convertToEntityAttribute("OPERACIONAL"));
        assertEquals(TipoUnidade.INTERMEDIARIA, converter.convertToEntityAttribute("INTERMEDIARIA"));
    }

    @Test
    @DisplayName("Deve lançar exceção para valor desconhecido")
    void deveLancarExcecaoParaValorDesconhecido() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("DESCONHECIDO"));
    }
}
