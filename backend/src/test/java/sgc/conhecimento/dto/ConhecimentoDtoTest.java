package sgc.conhecimento.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConhecimentoDtoTest {

    @Test
    void testConhecimentoDtoConstructorAndGettersSetters() {
        // Test constructor with parameters
        ConhecimentoDto dto = new ConhecimentoDto(1L, 100L, "Test Description");

        // Test getters
        assertEquals(1L, dto.codigo());
        assertEquals(100L, dto.atividadeCodigo());
        assertEquals("Test Description", dto.descricao());

        // Test setters (records are immutable, so we create a new instance)
        dto = new ConhecimentoDto(2L, 200L, "New Description");

        assertEquals(2L, dto.codigo());
        assertEquals(200L, dto.atividadeCodigo());
        assertEquals("New Description", dto.descricao());
    }

    void testConhecimentoDtoNoArgsConstructor() {
        // This test is invalid for a record and is the source of compilation errors.
        // It's being removed.
    }
}