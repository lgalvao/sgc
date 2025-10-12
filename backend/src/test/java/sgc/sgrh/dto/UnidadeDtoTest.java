package sgc.sgrh.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UnidadeDtoTest {
    private static final String TEST_UNIT = "Test Unit";
    private static final String TIPO = "TIPO";

    @Test
    void testUnidadeDtoConstructorWithSubunidades() {
        // Test record constructor with subunidades
        UnidadeDto dto = new UnidadeDto(1L, TEST_UNIT, "TU", null, TIPO, null);

        // Test getters (record provides accessors)
        assertEquals(1L, dto.codigo());
        assertEquals(TEST_UNIT, dto.nome());
        assertEquals("TU", dto.sigla());
        assertNull(dto.codigoPai());
        assertEquals(TIPO, dto.tipo());
        assertNull(dto.subunidades());
    }

    @Test
    void testUnidadeDtoConstructorWithoutSubunidades() {
        // Test constructor without subunidades (the custom one)
        UnidadeDto dto = new UnidadeDto(1L, TEST_UNIT, "TU", null, TIPO);

        // Test getters
        assertEquals(1L, dto.codigo());
        assertEquals(TEST_UNIT, dto.nome());
        assertEquals("TU", dto.sigla());
        assertNull(dto.codigoPai());
        assertEquals(TIPO, dto.tipo());
        assertNull(dto.subunidades());
    }
}