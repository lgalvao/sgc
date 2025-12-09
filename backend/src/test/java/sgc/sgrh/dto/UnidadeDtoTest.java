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
        UnidadeDto dto = new UnidadeDto(1L, TEST_UNIT, "TU", null, TIPO, false);

        // Test getters (record provides accessors)
        assertEquals(1L, dto.getCodigo());
        assertEquals(TEST_UNIT, dto.getNome());
        assertEquals("TU", dto.getSigla());
        assertNull(dto.getCodigoPai());
        assertEquals(TIPO, dto.getTipo());
        assertNull(dto.getSubunidades());
    }

    @Test
    void testUnidadeDtoConstructorWithoutSubunidades() {
        // Test constructor without subunidades (the custom one)
        UnidadeDto dto = new UnidadeDto(1L, TEST_UNIT, "TU", null, TIPO, false);

        // Test getters
        assertEquals(1L, dto.getCodigo());
        assertEquals(TEST_UNIT, dto.getNome());
        assertEquals("TU", dto.getSigla());
        assertNull(dto.getCodigoPai());
        assertEquals(TIPO, dto.getTipo());
        assertNull(dto.getSubunidades());
    }
}
