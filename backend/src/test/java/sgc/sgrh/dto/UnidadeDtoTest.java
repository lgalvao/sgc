package sgc.sgrh.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UnidadeDtoTest {

    @Test
    void testUnidadeDtoConstructorWithSubunidades() {
        // Test record constructor with subunidades
        UnidadeDto dto = new UnidadeDto(1L, "Test Unit", "TU", null, "TIPO", null);

        // Test getters (record provides accessors)
        assertEquals(1L, dto.codigo());
        assertEquals("Test Unit", dto.nome());
        assertEquals("TU", dto.sigla());
        assertNull(dto.codigoPai());
        assertEquals("TIPO", dto.tipo());
        assertNull(dto.subunidades());
    }

    @Test
    void testUnidadeDtoConstructorWithoutSubunidades() {
        // Test constructor without subunidades (the custom one)
        UnidadeDto dto = new UnidadeDto(1L, "Test Unit", "TU", null, "TIPO");

        // Test getters
        assertEquals(1L, dto.codigo());
        assertEquals("Test Unit", dto.nome());
        assertEquals("TU", dto.sigla());
        assertNull(dto.codigoPai());
        assertEquals("TIPO", dto.tipo());
        assertNull(dto.subunidades());
    }
}